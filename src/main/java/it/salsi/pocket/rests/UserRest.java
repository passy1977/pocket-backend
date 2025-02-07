/*
The MIT License (MIT)

Original Work Copyright (c) 2018-2025 Antonio Salsi

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package it.salsi.pocket.rests;

import it.salsi.commons.CommonsException;
import it.salsi.commons.utils.Crypto;
import it.salsi.pocket.ResponseEntityUtils;
import it.salsi.pocket.core.BaseRest;
import it.salsi.pocket.models.Device;
import it.salsi.pocket.models.User;
import it.salsi.pocket.repositories.DeviceRepository;
import it.salsi.pocket.repositories.UserRepository;
import it.salsi.pocket.security.PasswordEncoder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

import static it.salsi.pocket.Constant.*;
import static it.salsi.pocket.models.User.Status.ACTIVE;

@Deprecated
@Log
@RestController
@RequestMapping("${server.api-version}/user/")
public final class UserRest {

    @Value("${server.auth.user}")
    @Nullable
    private String baseAuthUser;

    @Value("${server.auth.passwd}")
    @Nullable
    private String baseAutPasswd;

    @NotNull
    private final UserRepository userRepository;

    @NotNull
    private final DeviceRepository deviceRepository;

    @NotNull
    private final Crypto crypto;

    @NotNull
    private final PasswordEncoder passwordEncoder;

    public UserRest(@Autowired @NotNull final UserRepository userRepository,
                    @Autowired @NotNull final DeviceRepository deviceRepository,
                    @Autowired @NotNull final Crypto crypto,
                    @Autowired @NotNull final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
        this.crypto = crypto;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/{uuid}/{version}/{email}/{passwd}")
    public ResponseEntity<Iterable<User>> login(@PathVariable @NotNull final String uuid,
                                           @PathVariable @NotNull final String version,
                                           @PathVariable @NotNull final String email,
                                           @PathVariable @NotNull final String passwd,
                                           @NotNull final HttpServletRequest request) throws CommonsException {
        final var userOptional = userRepository.findByEmailAndPasswd(email, crypto.decryptToString(passwd));
        userOptional.ifPresent(user -> {

            //TODO: complete login

//            if (user.getDevices().isEmpty()) {
//                user.getDevices().add(deviceRepository.save(newDevice(uuid, request.getRemoteAddr(), version, user)));
//
//                try {
//                    setEncryptedData(user);
//                } catch (CommonsException e) {
//                    log.severe(e.getMessage());
//                }
//
//            } else {
//
//                final var devices = user.getDevices().stream().filter(device -> device.getUuid().equals(uuid)).collect(Collectors.toSet());
//                devices.forEach(device -> {
//                    device.setToken(UUID.randomUUID().toString());
//                    device.updateDateTimeLastLogin();
//                    device.updateDateTimeLastUpdate();
//                    var remoteIP = request.getRemoteAddr();
//                    if (request.getHeader("x-forwarded-for") != null) {
//                        remoteIP = request.getHeader("x-forwarded-for");
//                    }
//                    device.setAddress(remoteIP);
//                    device.setVersion(version);
//                    deviceRepository.save(device);
//                });
//
//                if (devices.isEmpty()) {
//                    devices.add(deviceRepository.save(newDevice(uuid, request.getRemoteAddr(), version, user)));
//                }
//
//                try {
//                    setEncryptedData(user);
//                } catch (CommonsException e) {
//                    log.severe(e.getMessage());
//                }
//
//                user.setServerId(user.getId());
//                user.setId(0L);
//                user.setDevices(devices);
//            }

        });

        return userOptional.<ResponseEntity<Iterable<User>>>map(user ->
                ResponseEntity.ok(List.of(user))
        ).orElseGet(() ->
                ResponseEntityUtils.returnNonAuthoritativeInformation(List.of())
        );
    }


    @GetMapping("/lastLogin/{token}")
    public ResponseEntity<Iterable<Object>> getAll(@PathVariable @NotNull final String token) {
        final var device = deviceRepository.findByUuid(token);
        if (device.isPresent()) {
            if (device.get().getStatus() != Device.Status.ACTIVE) return ResponseEntityUtils.returnNonAuthoritativeInformation(List.of());
            if (device.get().getUser().getStatus() != User.Status.ACTIVE) return ResponseEntityUtils.returnNonAuthoritativeInformation(List.of());

            Map<String, Object> ret = new HashMap<>();
            ret.put("id", device.get().getUser().getId());

            ret.put("dateTimeLastUpdate", new SimpleDateFormat(DATE_TIME_FORMAT).format(device.get().getTimestampLastUpdate()));

            return ResponseEntity.ok(List.of(ret));
        } else return ResponseEntityUtils.returnNoContent(List.of());
    }


    @PostMapping
    public ResponseEntity<Iterable<User>> insert(@Valid @RequestBody @NotNull final Set<User> users) throws CommonsException {

        if (users.isEmpty()) {
            return ResponseEntityUtils.returnNoContent(List.of());
        }

        long idByEmail = 0;
        var user = users.stream().findFirst().orElseThrow();

        //TODO: handle this situation
//        if (userRepository.findByEmailAndPasswd(user.getHostAuthUser(), crypto.decryptToString(user.getHostAuthPasswd())).isEmpty()) {
//            return ResponseEntityUtils.returnNonAuthoritativeInformation(List.of(User.build(EMAIL_HOST_WRONG_CREDENTIAL.value)));
//        }

        final var optional = userRepository.findByEmail(user.getEmail());
        if (optional.isPresent()) {
            final var userByEmail = optional.get();
            idByEmail = userByEmail.getId();
            userByEmail.setStatus(ACTIVE);
            if (userByEmail.getEmail().equals(user.getEmail())) {
                return ResponseEntityUtils.returnNonAuthoritativeInformation(List.of(User.build(EMAIL_ALREADY_USED.value)));
            }
        }

        user.setPasswd(crypto.decryptToString(user.getPasswd()));

        final var userToRet = userRepository.save(user);
        //TODO: handle this situation
//        userToRet.setServerId(userToRet.getId());
//        userToRet.setId(idByEmail);

        try {
            setEncryptedData(userToRet);
        } catch (CommonsException e) {
            log.severe(e.getMessage());
        }

        return ResponseEntity.ok(List.of(userToRet));
    }

    @DeleteMapping("/{token}")
    public void logout(@PathVariable final String token) {
        deviceRepository.findByUuid(token).ifPresent(device -> {
            device.setUser(null);
            deviceRepository.save(device);

            //TODO: handle this situation
            //deviceRepository.deleteByToken(token);
        });

    }

    @ExceptionHandler
    public void exceptionHandler(@NotNull final Exception e, @NotNull final HttpServletResponse response) throws Exception {
        BaseRest.exceptionHandler(e, response);
    }


    private void setEncryptedData(@NotNull final User user) throws CommonsException {
        //TODO: handle this situation
//        if (baseAuthUser != null) {
//            user.setHostAuthUser(baseAuthUser);
//        }
//
//        if (baseAutPasswd != null) {
//            user.setHostAuthPasswd(crypto.encryptToString(passwordEncoder.encode(baseAutPasswd)));
//        } else {
//            user.setHostAuthPasswd("");
//        }
    }

}
