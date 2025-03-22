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
import it.salsi.commons.utils.NumberUtils;
import it.salsi.pocket.models.Device;
import it.salsi.pocket.models.Property;
import it.salsi.pocket.models.User;
import it.salsi.pocket.repositories.*;
import it.salsi.pocket.security.EncoderHelper;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

import static it.salsi.pocket.Constant.*;

@Log
@Service
public final class DatabaseManagerImpl implements DatabaseManager {

    @Value("${server.auth.user}")
    @Nullable
    private String authUser;

    @Value("${server.auth.passwd}")
    @Nullable
    private String authPasswd;

    @NotNull
    private final UserRepository userRepository;

    @NotNull
    private final DeviceRepository deviceRepository;

    @NotNull
    private final PropertyRepository propertyRepository;

    @NotNull
    private final GroupRepository groupRepository;

    @NotNull
    private final GroupFieldRepository groupFieldRepository;

    @NotNull
    private final FieldRepository fieldRepository;

    @NotNull
    private final EncoderHelper encoderHelper;

    public DatabaseManagerImpl(@Autowired @NotNull final UserRepository userRepository,
                               @Autowired @NotNull final DeviceRepository deviceRepository,
                               @Autowired @NotNull final PropertyRepository propertyRepository,
                               @Autowired @NotNull final GroupRepository groupRepository,
                               @Autowired @NotNull final GroupFieldRepository groupFieldRepository,
                               @Autowired @NotNull final FieldRepository fieldRepository,
                               @Autowired @NotNull final EncoderHelper encoderHelper
    ) {
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
        this.propertyRepository = propertyRepository;
        this.groupRepository = groupRepository;
        this.groupFieldRepository = groupFieldRepository;
        this.fieldRepository = fieldRepository;
        this.encoderHelper = encoderHelper;
    }


    @Override
    public void init() throws CommonsException {
        if(authUser == null || authPasswd == null) {
            throw new CommonsException("authUser or authPasswd not set");
        }
        if(authPasswd.length() != 32) {
            throw new CommonsException("authPasswd must be 32 byte");
        }

        AtomicReference<User> adminUser = new AtomicReference<>(new User());
        userRepository.findByEmail(authUser).ifPresent(adminUser::set);

        propertyRepository.getByUserIdAndKey(adminUser.get().getId(), PROPERTY_DB_VERSION).ifPresentOrElse(property -> {
            Integer version = (Integer) PROPERTY_DB_VERSION.getMetaProperty().defaultValue();
            if (version == null) {
                return;
            }
            try {
                if (Integer.parseInt(property.getValue()) < version) {
                    updateVersion(adminUser.get(), version);
                }
            } catch (NumberFormatException e) {
                updateVersion(adminUser.get(), NumberUtils.parseInt(PROPERTY_DB_VERSION.value));
            }

        }, () ->
                updateVersion(adminUser.get(), 0)
        );

    }

    @Override
    public void cleanOldData()  throws CommonsException {
        log.info("Start delete data");
        if(authUser == null || authPasswd == null) {
            throw new CommonsException("authUser or authPasswd not set");
        }
        if(authPasswd.length() != 32) {
            throw new CommonsException("authPasswd must be 32 byte");
        }

        AtomicReference<User> adminUser = new AtomicReference<>(new User());
        userRepository.findByEmail(authUser).ifPresent(adminUser::set);

        propertyRepository.getByUserIdAndKey(adminUser.get().getId(), PROPERTY_CLEAN_DATA_ENABLE).ifPresentOrElse(invalidatorEnable -> {

            if (Boolean.TRUE.toString().equals(invalidatorEnable.getValue())) {
                try {
                    propertyRepository.getByUserIdAndKey(adminUser.get().getId(), PROPERTY_INVALIDATOR_MAX_LOGIN_DAYS).ifPresentOrElse(invalidatorMaxLoginDays -> {
                        log.info("Start delete data thread: " + Thread.currentThread().getName());

                        userRepository.findAll().forEach(user -> {
                            if (user.getStatus() != User.Status.DELETED) {
                                if(user.getDevices().isEmpty()) {
                                    return;
                                }

                                var timestampLastUpdate = 0L;
                                for(final var device : user.getDevices()) {
                                    if(device.getTimestampLastUpdate() > timestampLastUpdate ) {
                                        timestampLastUpdate = device.getTimestampLastUpdate();
                                    }
                                }


                                if (deviceRepository.countAllByUserAndTimestampLastUpdateBeforeAndStatusIsNot(user, timestampLastUpdate, Device.Status.INVALIDATED) == 0) {
                                    log.info("Start deleting data, user:" + user.getEmail() + "timestampLastUpdate:" + timestampLastUpdate);

                                    fieldRepository.findByUserAndDeletedAndTimestampLastUpdateLessThan(user, true, timestampLastUpdate).forEach(fieldRepository::delete);
                                    groupFieldRepository.findByUserAndDeletedAndTimestampLastUpdateLessThan(user, true, timestampLastUpdate).forEach(groupFieldRepository::delete);
                                    groupRepository.findByUserAndDeletedAndTimestampLastUpdateLessThan(user, true, timestampLastUpdate).forEach(groupRepository::delete);

                                    log.info("End deleting data");
                                }

                            }
                        });


                    }, () -> log.severe("cron delete data not stared: invalid conversion date"));

                } catch (NumberFormatException e) {
                    log.severe("cron delete data not stared: invalid conversion date");
                }
            }
        }, () -> log.warning("cron delete data disabled"));


        log.info("End delete data");
    }

    private void updateVersion(@NotNull final User user, final int version) {
        switch (version) {
            case 0, 1, 2 -> {
                final var metaProperty = PROPERTY_DB_VERSION.getMetaProperty();
                final var property = new Property();
                property.setUser(user);
                property.setKey(PROPERTY_DB_VERSION);
                property.setValue(metaProperty.defaultValue());
                propertyRepository.save(property);
            }
            default -> System.out.print("At the last version");
        }
    }
}
