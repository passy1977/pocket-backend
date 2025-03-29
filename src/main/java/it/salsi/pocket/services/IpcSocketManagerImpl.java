/***************************************************************************
 *
 * Pocket web backend
 * Copyright (C) 2018/2025 Antonio Salsi <passy.linux@zresa.it>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 ***************************************************************************/

package it.salsi.pocket.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.salsi.commons.CommonsException;
import it.salsi.pocket.models.Device;
import it.salsi.pocket.models.User;
import it.salsi.pocket.repositories.DeviceRepository;
import it.salsi.pocket.repositories.UserRepository;
import it.salsi.pocket.security.EncoderHelper;
import it.salsi.pocket.security.RSAHelper;
import lombok.Setter;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static it.salsi.pocket.Constant.DIVISOR;
import static it.salsi.pocket.security.RSAHelper.ALGORITHM;
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


    private static class DeviceExtended extends Device {


        @JsonProperty("userId")
        private final @NotNull Long _userId;

        @JsonProperty("host")
        private final @Nullable String _host;

        @JsonProperty("hostPublicKey")
        private @NotNull String _publicKey;

        @JsonProperty("aesCbcIv")
        private @NotNull String aesCbcIv;

        public DeviceExtended(@NotNull final Device device, @Nullable final String host, @NotNull final String aesCbcIv) {
            setId(device.getId());
            setUuid(device.getUuid());
            setStatus(device.getStatus());
            setTimestampLastUpdate(device.getTimestampLastUpdate());
            setTimestampLastLogin(device.getTimestampLastLogin());
            setNote(device.getNote());
            this._userId = device.getUser().getId();
            this._host = host;
            try {

                _publicKey = "-----BEGIN PUBLIC KEY-----\n";
                _publicKey += device.getPublicKey() + "\n";
                _publicKey += "-----END PUBLIC KEY-----\n";
            } catch (CommonsException e) {
                log.severe(e.getMessage());
                _publicKey = "";
            }
            this.aesCbcIv = aesCbcIv;
        }


    }
    static public final int SOCKET_PORT = 8333;

    private boolean loop = true;

    private @NotNull final DeviceRepository deviceRepository;

    private @NotNull final UserRepository userRepository;

    private @NotNull final EncoderHelper encoderHelper;

    @Value("${server.url}")
    @Nullable
    private String serverUrl;

    @Value("${server.auth.passwd}")
    @Nullable
    private String authPasswd;

    @Value("${server.socket-port}")
    @Nullable
    private Integer socketPort;

    @Value("${server.aes.cbc.iv}")
    @Nullable
    private String aesCrbIv;

    private @Nullable String passwd;

    public IpcSocketManagerImpl(
            @Autowired @NotNull final DeviceRepository deviceRepository,
            @Autowired @NotNull final UserRepository userRepository,
            @Autowired @NotNull final EncoderHelper encoderHelper
    ) {
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
        this.encoderHelper = encoderHelper;
        if(socketPort == null) {
            socketPort = SOCKET_PORT;
        }
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

                ret = new User(split[3], email, encoderHelper.encode(split[2]));

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
                ret.setPasswd(encoderHelper.encode(split[2]));

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
    private @NotNull Optional<DeviceExtended>  handleDevice(@NotNull final PrintWriter out, final String @NotNull [] split) {
        if(split.length < 3) {
            out.println(WRONG_PARAMS.value);
            return Optional.empty();
        }
        String cmd = split[0];
        String email = split[1];
        String passwd = split[2];
        String uuid = split.length >= 4 ? split[3] : "";



        final var atmDevice = new AtomicReference<Optional<Device>>(Optional.empty());
        final var user = new AtomicReference<User>(null);

        userRepository.findByEmailAndPasswd(email, encoderHelper.encode(passwd)).ifPresent( u-> {
            user.set(u);
            atmDevice.set(deviceRepository.findByUserAndUuid(u, uuid));
        });

        if(user.get() == null) {
            out.println(USER_NOT_EXIST.value);
            return Optional.empty();
        }

        Device ret = null;

        String privateKey = "";

        switch (cmd) {
            case "ADD_DEVICE":
                if(atmDevice.get().isPresent()) {
                    out.println(DEVICE_ALREADY_EXIST.value);
                    return Optional.empty();
                }
                ret = new Device(user.get());

                try {
                    var rsaHelper = new RSAHelper(ALGORITHM, RSAHelper.KEY_SIZE);
                    rsaHelper.enroll();

                    ret.setPrivateKey(Objects.requireNonNull(rsaHelper.getPrivateKeyString()));
                    ret.setPublicKey(Objects.requireNonNull(rsaHelper.getPublicKeyString()));

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


        if(ret == null) {
            return Optional.empty();
        }
        assert aesCrbIv != null;
        return Optional.of(new DeviceExtended(ret, serverUrl, aesCrbIv));
    }

    /**
     * ADD_USER|test@test.com|pwd|user
     * MOD_USER|test@test.com|pwd1|user1
     * RM_USER|test@test.com
     * GET_USER|test@test.com
     *
     * ADD_DEVICE|test@test.com|12345678123456781234567812345678
     * RM_DEVICE|test@test.com|pwd|47a48e92-c521-4f07-a4b3-757c889a0816
     * GET_DEVICE|test@test.com|pwd|47a48e92-c521-4f07-a4b3-757c889a0816
     */
    @Async
    @Override
    public void start() {

        log.info("Start socket");
        passwd = null;

        assert authPasswd != null;
        assert authPasswd.length() == 32;
        assert socketPort != null;
        try (final var serverSocket = new ServerSocket(socketPort, 0, InetAddress.getByName(null))) {

            while (loop && !serverSocket.isClosed()) {

                final var client = serverSocket.accept();

                try(final var in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {

                    try(final var out = new PrintWriter(client.getOutputStream(), true)) {

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

                                final var split = Arrays
                                        .stream(line.split("["+DIVISOR.value+"]"))
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


        } catch (IOException e) {
            log.severe(e.getMessage());
        } finally {
            try {
                Thread.sleep(1_000);
            } catch (InterruptedException ex) {
                log.severe(ex.getMessage());
            }
            start();
        }

        log.info("End socket");
    }
}
