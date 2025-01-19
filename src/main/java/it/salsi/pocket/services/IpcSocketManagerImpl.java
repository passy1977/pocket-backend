package it.salsi.pocket.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.salsi.commons.CommonsException;
import it.salsi.pocket.models.Device;
import it.salsi.pocket.models.User;
import it.salsi.pocket.repositories.DeviceRepository;
import it.salsi.pocket.repositories.UserRepository;
import it.salsi.pocket.security.PasswordEncoder;
import it.salsi.pocket.security.RSAHelper;
import lombok.Setter;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static it.salsi.pocket.Constant.*;
import static it.salsi.pocket.services.IpcSocketManagerImpl.Response.*;

@Setter
@Log
@Service
public class IpcSocketManagerImpl implements IpcSocketManager {

    public enum Response {
        OK(0),
        ERROR(1),
        WRONG_PARAMS(2),
        USER_ALREADY_EXIST(3),
        DEVICE_ALREADY_EXIST(4),
        USER_NOT_EXIST(5),
        DEVICE_NOT_EXIST(6),
        WRONG_PASSWD(7);


        Response(int value) {
            this.value = value;
        }

        public int value = 0;
    }

    private boolean loop = true;

    private @NotNull final DeviceRepository deviceRepository;

    private @NotNull final UserRepository userRepository;

    private @NotNull final PasswordEncoder passwordEncoder;

    @Value("${basic.auth.passwd}")
    @Nullable
    private String authPasswd;

    private @Nullable String passwd;

    public IpcSocketManagerImpl(
            @Autowired @NotNull final DeviceRepository deviceRepository,
            @Autowired @NotNull final UserRepository userRepository,
            @Autowired @NotNull final PasswordEncoder passwordEncoder
    ) {
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private @NotNull Optional<User> handleUser(@NotNull final PrintWriter out, final String @NotNull [] split) {
        if(split.length < 2) {
            out.println(WRONG_PARAMS.value);
            return Optional.empty();
        }
        String cmd = split[0];
        String email = split[1];

        var optUser = userRepository.findByEmail(email);

        User ret = null;

        switch (cmd) {
            case "ADD_USER":
                if(optUser.isPresent()) {
                    out.println(USER_ALREADY_EXIST.value);
                    return Optional.empty();
                } else if(split.length < 4) {
                    out.println(WRONG_PARAMS.value);
                    return Optional.empty();
                }

                ret = new User(split[3], email, passwordEncoder.encode(split[2]));

                userRepository.save(ret);
                break;
            case "MOD_USER":
                if(optUser.isEmpty()) {
                    out.println(USER_NOT_EXIST.value);
                    return Optional.empty();
                } else if(split.length < 4) {
                    out.println(WRONG_PARAMS.value);
                    return Optional.empty();
                }

                ret = optUser.get();
                ret.setName(split[3]);
                ret.setEmail(email);
                ret.setPasswd(passwordEncoder.encode(split[2]));

                userRepository.save(ret);

                break;
            case "RM_USER":
                if(optUser.isEmpty()) {
                    out.println(USER_NOT_EXIST.value);
                    return Optional.empty();
                }

                ret = optUser.get();
                userRepository.delete(ret);

                break;
            case "GET_USER":
                if(optUser.isEmpty()) {
                    out.println(USER_NOT_EXIST.value);
                    return Optional.empty();
                }

                ret = optUser.get();
                break;
            default:
                out.println(ERROR.value);
        }


        return Optional.ofNullable(ret);
    }

    //cmd|email|uuid
    private @NotNull Optional<Device>  handleDevice(@NotNull final PrintWriter out, final String @NotNull [] split) {
        if(split.length < 2) {
            out.println(WRONG_PARAMS.value);
            return Optional.empty();
        }
        String cmd = split[0];
        String email = split[1];
        String uuid = split.length >= 3 ? split[2] : "";

        final var atmDevice = new AtomicReference<Optional<Device>>(Optional.empty());
        final var user = new AtomicReference<User>(null);

        AtomicBoolean stop = new AtomicBoolean(false);
        userRepository.findByEmail(email).ifPresentOrElse( u-> {
            user.set(u);
            atmDevice.set(deviceRepository.findByUserAndUuid(u, uuid));
        }, () -> stop.set(true));

        if(stop.get()) {
            return Optional.empty();
        }

        if(user.get() == null) {
            out.println(USER_NOT_EXIST.value);
            return Optional.empty();
        }

        Device ret = null;

        switch (cmd) {
            case "ADD_DEVICE":
                if(atmDevice.get().isPresent()) {
                    out.println(DEVICE_ALREADY_EXIST.value);
                    return Optional.empty();
                }
                ret = new Device(user.get());

                try {
                    var rsaHelper = new RSAHelper("RSA", 2048);
                    rsaHelper.enroll();
                    ret.setPrivateKey(rsaHelper.getPrivateKey());
                    ret.setPublicKey(rsaHelper.getPublicKey());
                } catch (CommonsException e) {
                    out.println(e.getMessage());
                    out.println(0);
                    return Optional.empty();
                }

                ret = deviceRepository.save(ret);
                break;
            case "RM_DEVICE":
                if(atmDevice.get().isEmpty()) {
                    out.println(DEVICE_NOT_EXIST.value);
                    return Optional.empty();
                }

                ret = atmDevice.get().get();
                deviceRepository.delete(ret);

                break;
            case "GET_DEVICE":
                if(atmDevice.get().isEmpty()) {
                    out.println(DEVICE_NOT_EXIST.value);
                    return Optional.empty();
                }

                ret = atmDevice.get().get();
                break;
            default:
                out.println(ERROR.value);
        }


        return Optional.ofNullable(ret);
    }

    /**
     * ADD_USER|test@test.it|pwd|user
     * MOD_USER|test@test.it|pwd1|user1
     * RM_USER|test@test.it
     * GET_USER|test@test.it
     *
     * ADD_DEVICE|test@test.it
     * RM_DEVICE|test@test.it|47a48e92-c521-4f07-a4b3-757c889a0816
     * GET_DEVICE|test@test.it|47a48e92-c521-4f07-a4b3-757c889a0816
     */
    @Async
    @Override
    public void start() {

        log.info("start socket");

        try (final var serverSocket = new ServerSocket(SOCKET_PORT, 0, InetAddress.getByName(null))) {

            while (loop && !serverSocket.isClosed()) {

                final var client = serverSocket.accept();

                try(final var in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {

                    try(final var out = new PrintWriter(client.getOutputStream(), true)) {

                        if(authPasswd == null) {
                            out.println("Auth passwd non set");
                            continue;
                        }

                        while(loop && !client.isClosed()) {

                            // Check for client disconnection
                            if(in.read() == -1) {
                                passwd = null;

                                System.out.println("client disconnected. Socket closing...");
                                in.close();
                            }


                            String line;
                            while (loop && (line = in.readLine()) != null) {

                                if(passwd == null) {
                                    if(line.equals(authPasswd)) {
                                        passwd = line;
                                        out.println(0);
                                    } else {
                                        out.println(WRONG_PASSWD.value);
                                    }
                                    continue;
                                }

                                if(authPasswd != null) {
                                    final var split = Arrays
                                            .stream(line.split("[|]"))
                                            .map(String::trim)
                                            .toArray(String[]::new);

                                    if(split[0].contains("_USER")) {
                                        handleUser(out, split).ifPresent( u -> {
                                            final var mapper = new ObjectMapper();
                                            try {
                                                out.println(mapper.writeValueAsString(u));
                                                out.println(0);
                                            } catch (JsonProcessingException e) {
                                                out.println(e.getMessage());
                                                out.println(ERROR.value);
                                            }
                                        });
                                    } else if(split[0].contains("_DEVICE")) {
                                        handleDevice(out, split).ifPresent( d -> {
                                            final var mapper = new ObjectMapper();
                                            try {
                                                out.println(mapper.writeValueAsString(d));
                                                out.println(0);
                                            } catch (JsonProcessingException e) {
                                                out.println(e.getMessage());
                                                out.println(ERROR.value);
                                            }
                                        });
                                    }

                                }
                            }
                        }
                    }
                }
            }


        } catch (IOException e) {
            log.severe(e.getMessage());
            Thread.currentThread().interrupt();
        }

        log.info("end socket");
    }
}
