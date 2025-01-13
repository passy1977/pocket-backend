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
import it.salsi.pocket.models.Property;
import it.salsi.pocket.models.User;
import it.salsi.pocket.repositories.*;
import it.salsi.pocket.security.PasswordEncoder;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.config.ConfigDataException;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

import static it.salsi.pocket.Constant.*;

@Log
@Service
public final class DatabaseManagerImpl implements DatabaseManager {

    @Value("${basic.auth.user}")
    @Nullable
    private String authUser;

    @Value("${basic.auth.passwd}")
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
    private final PasswordEncoder passwordEncoder;

    public DatabaseManagerImpl(@Autowired @NotNull final UserRepository userRepository,
                               @Autowired @NotNull final DeviceRepository deviceRepository,
                               @Autowired @NotNull final PropertyRepository propertyRepository,
                               @Autowired @NotNull final GroupRepository groupRepository,
                               @Autowired @NotNull final GroupFieldRepository groupFieldRepository,
                               @Autowired @NotNull final FieldRepository fieldRepository,
                               @Autowired @NotNull final PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
        this.propertyRepository = propertyRepository;
        this.groupRepository = groupRepository;
        this.groupFieldRepository = groupFieldRepository;
        this.fieldRepository = fieldRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public void init() throws CommonsException {
        if(authUser == null || authPasswd == null) {
            throw new CommonsException("authUser or authPasswd not set");
        }

        AtomicReference<User> adminUser = new AtomicReference<>(new User());
        userRepository.findByEmail(authUser).ifPresentOrElse(
                adminUser::set,
                () -> adminUser.set(userRepository.save(new User(authUser, authUser, passwordEncoder.encode(authPasswd))))
        );

        propertyRepository.getByUserIdAndKey(adminUser.get().getId(), PROPERTY_DB_VERSION.value).ifPresentOrElse(property -> {
            Integer version = (Integer) PROPERTY_DB_VERSION.getMetaProperty().defaultValue();
            if (version == null) {
                return;
            }
            try {
                if (Integer.parseInt(property.getValue()) < version) {
                    updateVersion(version);
                }
            } catch (NumberFormatException e) {
                updateVersion(0);
            }


        }, () -> updateVersion(0));

    }

    @Override
    public void cleanOldData() {
        log.info("start delete data");

        //userRepository.findByEmail(authUser);

        propertyRepository.getByKey(PROPERTY_CLEAN_DATA_ENABLE).ifPresentOrElse(invalidatorEnable -> {

            if (Boolean.TRUE.toString().equals(invalidatorEnable.getValue())) {
                try {
                    propertyRepository.getByKey(PROPERTY_INVALIDATOR_MAX_LOGIN_DAYS).ifPresentOrElse(invalidatorMaxLoginDays -> {
                        log.info("start delete data thread: " + Thread.currentThread().getName());

                        userRepository.findAll().forEach(user -> {
                            if (user.getStatus() != User.Status.DELETED) {

                                if (deviceRepository.countAllByUserAndDateTimeLastUpdateBeforeAndStatusIsNot(user, user.getDateTimeLastUpdate(), Device.Status.INVALIDATED) == 0) {
                                    log.info("start deleting data");

                                    fieldRepository.findByUserAndDeletedAndDateTimeLastUpdateLessThan(user, true, user.getDateTimeLastUpdate()).forEach(fieldRepository::delete);
                                    groupFieldRepository.findByUserAndDeletedAndDateTimeLastUpdateLessThan(user, true, user.getDateTimeLastUpdate()).forEach(groupFieldRepository::delete);
                                    groupRepository.findByUserAndDeletedAndDateTimeLastUpdateLessThan(user, true, user.getDateTimeLastUpdate()).forEach(groupRepository::delete);

                                    log.info("end deleting data");
                                }

                            }
                        });


                    }, () -> log.severe("cron delete data not stared: invalid conversion date"));

                } catch (NumberFormatException e) {
                    log.severe("cron delete data not stared: invalid conversion date");
                }
            }
        }, () -> log.warning("cron delete data disabled"));


        log.info("end delete data");
    }

    private void updateVersion(final int version) {
        switch (version) {
            case 0, 1, 2 -> {
                final var metaProperty = PROPERTY_DB_VERSION.getMetaProperty();
                final var property = new Property();
                property.setKey(PROPERTY_DB_VERSION);
                property.setValue(metaProperty.defaultValue());
                propertyRepository.save(property);
            }
            default -> System.out.printf("At the last version");
        }
    }
}
