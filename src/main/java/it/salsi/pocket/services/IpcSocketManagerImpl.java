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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
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
        DEVICE_NOT_EXIST(6);


        Response(int value) {
            this.value = value;
        }

        public int value = 0;
    }

    private boolean loop = true;

    private @NotNull final DeviceRepository deviceRepository;

    private @NotNull final UserRepository userRepository;

    private @NotNull final PasswordEncoder passwordEncoder;

    public IpcSocketManagerImpl(
            @Autowired @NotNull final DeviceRepository deviceRepository,
            @Autowired @NotNull final UserRepository userRepository,
            @Autowired @NotNull final PasswordEncoder passwordEncoder
    ) {
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //cmd|email|passwd|name
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

        AtomicReference<Optional<Device>> atmDevice = new AtomicReference<>(Optional.empty());
        final var user = new AtomicReference<User>();

        AtomicBoolean stop = new AtomicBoolean(false);
        userRepository.findByEmail(email).ifPresentOrElse( u-> {
            user.set(u);
            atmDevice.set(deviceRepository.findByUser(u));
        }, () -> stop.set(true));

        if(stop.get()) {
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

                deviceRepository.save(ret);
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


    @Async
    @Override
    public void start() {

        log.info("start socket");

        try (final var serverSocket = new ServerSocket(SOCKET_PORT)) {

            while (loop) {
                final var client = serverSocket.accept();

                final var out = new PrintWriter(client.getOutputStream(), true);
                final var in = new BufferedReader(new InputStreamReader(client.getInputStream()));



                String line;
                while ((line = in.readLine()) != null) {

                    final var split = Arrays
                            .stream(line.split("[|]"))
                            .map(String::trim)
                            .toArray(String[]::new);

                    if(split[0].contains("_USER")) {
                        handleUser(out, split).ifPresent( u -> {
                            final var mapper = new ObjectMapper();
                            try {
                                mapper.writeValueAsString(u);
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
                                mapper.writeValueAsString(d);
                                out.println(0);
                            } catch (JsonProcessingException e) {
                                out.println(e.getMessage());
                                out.println(ERROR.value);
                            }
                        });
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
