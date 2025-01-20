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

package it.salsi.pocket.services;


import it.salsi.commons.CommonsException;
import it.salsi.pocket.models.Device;
import it.salsi.pocket.models.User;
import it.salsi.pocket.repositories.DeviceRepository;
import it.salsi.pocket.repositories.PropertyRepository;
import it.salsi.pocket.repositories.UserRepository;
import it.salsi.pocket.security.PasswordEncoder;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicReference;

import static it.salsi.pocket.Constant.PROPERTY_INVALIDATOR_ENABLE;
import static it.salsi.pocket.Constant.PROPERTY_INVALIDATOR_MAX_LOGIN_DAYS;
import static java.time.Instant.*;

@Log
@Service
public final class DevicesManagerImpl implements DevicesManager {

    @Value("${server.auth.user}")
    @Nullable
    private String authUser;

    @Value("${server.auth.passwd}")
    @Nullable
    private String authPasswd;

    @NotNull
    private final PropertyRepository propertyRepository;

    @NotNull
    private final DeviceRepository deviceRepository;

    @NotNull
    private final UserRepository userRepository;

    @NotNull
    private final PasswordEncoder passwordEncoder;

    public DevicesManagerImpl(@Autowired @NotNull final PropertyRepository propertyRepository,
                              @Autowired @NotNull final DeviceRepository deviceRepository,
                              @Autowired @NotNull final UserRepository userRepository,
                              @Autowired @NotNull final PasswordEncoder passwordEncoder) {
        this.propertyRepository = propertyRepository;
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public void invalidateAll() throws CommonsException {
        if(authUser == null || authPasswd == null) {
            throw new CommonsException("authUser or authPasswd not set");
        }

        log.info("start invalidate");

        AtomicReference<User> adminUser = new AtomicReference<>(new User());
        userRepository.findByEmail(authUser).ifPresentOrElse(
                adminUser::set,
                () -> adminUser.set(userRepository.save(new User(authUser, authUser, passwordEncoder.encode(authPasswd))))
        );


        propertyRepository.getByUserIdAndKey(adminUser.get().getId(), PROPERTY_INVALIDATOR_ENABLE).ifPresentOrElse(invalidatorEnable -> {
            if (Boolean.TRUE.toString().equals(invalidatorEnable.getValue())) {
                try {
                    propertyRepository.getByUserIdAndKey(adminUser.get().getId(), PROPERTY_INVALIDATOR_MAX_LOGIN_DAYS).ifPresentOrElse(invalidatorMaxLoginDays -> {
                        log.info("start invalidator thread: " + Thread.currentThread().getName());

                        try {
                            final var maxLoginDays = Integer.parseInt(invalidatorMaxLoginDays.getValue());

                            for (final var device : deviceRepository.findAll()) {

                                if (ChronoUnit.DAYS.between(ofEpochSecond(device.getTimestampLastUpdate()), ofEpochSecond(device.getTimestampLastUpdate())) > maxLoginDays) {
                                    device.setStatus(Device.Status.INVALIDATED);
                                    deviceRepository.save(device);
                                }

                            }

                        } catch (NumberFormatException e) {
                            log.severe("cron invalidator not stared: invalid conversion date");
                        }

                    }, () -> log.severe("cron invalidator not stared: invalid conversion date"));


                } catch (NumberFormatException e) {
                    log.severe("cron invalidator disabled");
                }

            } else {
                log.warning("cron invalidator not stared: invalid date");
            }

        }, () -> log.warning("cron invalidator disabled"));

        log.info("end invalidate");
    }

}
