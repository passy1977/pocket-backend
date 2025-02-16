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
import static java.time.Instant.ofEpochSecond;

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

        log.info("Start invalidate");

        AtomicReference<User> adminUser = new AtomicReference<>(new User());
        userRepository.findByEmail(authUser).ifPresentOrElse(
                adminUser::set,
                () -> adminUser.set(userRepository.save(new User(authUser, authUser, passwordEncoder.encode(authPasswd))))
        );


        propertyRepository.getByUserIdAndKey(adminUser.get().getId(), PROPERTY_INVALIDATOR_ENABLE).ifPresentOrElse(invalidatorEnable -> {
            if (Boolean.TRUE.toString().equals(invalidatorEnable.getValue())) {
                try {
                    propertyRepository.getByUserIdAndKey(adminUser.get().getId(), PROPERTY_INVALIDATOR_MAX_LOGIN_DAYS).ifPresentOrElse(invalidatorMaxLoginDays -> {
                        log.info("Start invalidator thread: " + Thread.currentThread().getName());

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

        log.info("End invalidate");
    }

}
