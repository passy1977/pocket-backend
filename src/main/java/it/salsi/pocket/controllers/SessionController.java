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

package it.salsi.pocket.controllers;

import it.salsi.commons.CommonsException;
import it.salsi.commons.messages.Success;
import it.salsi.pocket.models.Container;
import it.salsi.pocket.models.Device;
import it.salsi.pocket.models.User;
import it.salsi.pocket.repositories.DeviceRepository;
import it.salsi.pocket.repositories.UserRepository;
import it.salsi.pocket.security.PasswordEncoder;
import it.salsi.pocket.security.RSAHelper;
import it.salsi.pocket.services.CacheManager;
import it.salsi.pocket.services.CacheManager.CacheRecord;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static it.salsi.pocket.Constant.DIVISOR;
import static it.salsi.pocket.controllers.SessionController.ErrorCode.*;
import static it.salsi.pocket.security.RSAHelper.ALGORITHM;
import static it.salsi.pocket.security.RSAHelper.KEY_SIZE;

@Log
@Service
public class SessionController {

    enum ErrorCode {

        USER_NOT_FOUND(600),
        WRONG_SIZE_TOKEN(601),
        DEVICE_ID_NOT_MATCH(602),
        DEVICE_NOT_FOUND(603),
        SECRET_NOT_MATCH(604),
        USER_ID_NOT_MATCH(605),
        TIMESTAMP_LAST_UPDATE_NOT_MATCH(606),
        CACHE_NOT_FOND(607),
        SECRET_EMPTY(608),
        TIMESTAMP_LAST_NOT_PARSABLE(609),
        OK(200);

        ErrorCode(int code) {
            this.code = code;
        }

        public final int code;
    }


    private final @NotNull UserRepository userRepository;
    private final @NotNull DeviceRepository deviceRepository;
    private final @NotNull GroupController groupController;
    private final @NotNull GroupFieldController groupFieldController;
    private final @NotNull FieldController fieldController;
    private final @NotNull PasswordEncoder passwordEncoder;
    private final @NotNull CacheManager cacheManager;

    @Value("${server.check-timestamp-last-update}")
    @Nullable
    private Boolean checkTimestampLastUpdate;

    public SessionController(
            @Autowired @NotNull final UserRepository userRepository,
            @Autowired @NotNull final DeviceRepository deviceRepository,
            @Autowired @NotNull final GroupController groupController,
            @Autowired @NotNull final GroupFieldController groupFieldController,
            @Autowired @NotNull final FieldController fieldController,
            @Autowired @NotNull final PasswordEncoder passwordEncoder,
            @Autowired @NotNull final CacheManager cacheManager
    ) {
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
        this.groupController = groupController;
        this.groupFieldController = groupFieldController;
        this.groupFieldController.setGroupMapId(groupController.getMapId());
        this.fieldController = fieldController;
        this.fieldController.setGroupMapId(groupController.getMapId());
        this.fieldController.setGroupFieldMapId(groupFieldController.getMapId());
        this.passwordEncoder = passwordEncoder;
        this.cacheManager = cacheManager;
    }

    public @NotNull ResponseEntity<Container> getData(@NotNull final String uuid,
                                                      @NotNull final String crypt) throws CommonsException {


        final var now = Instant.now(Clock.systemUTC()).getEpochSecond();

        long timestampLastUpdate = 0;
        Optional<User> optUser = Optional.empty();
        Device device = null;
        RSAHelper rsaHelper = null;
        if(cacheManager.has(uuid)) {
            cacheManager.rm(uuid);
        }

        final var optDevice = deviceRepository.findByUuid(uuid);
        if(optDevice.isEmpty()) {
            return ResponseEntity.status(DEVICE_NOT_FOUND.code).build();
        }

        device = optDevice.get();
        rsaHelper = new RSAHelper(ALGORITHM, KEY_SIZE);
        rsaHelper.loadPublicKey(Base64.getDecoder().decode(device.getPublicKey()));
        rsaHelper.loadPrivateKey(Base64.getDecoder().decode(device.getPrivateKey()));

        final var decryptSplit = rsaHelper.decryptFromURLBase64(crypt).split("["+DIVISOR.value+"]");
        if(decryptSplit.length != 5)
        {
            return ResponseEntity.status(WRONG_SIZE_TOKEN.code).build();
        }

        if(Long.parseLong(decryptSplit[0]) != device.getId())
        {
            return ResponseEntity.status(DEVICE_ID_NOT_MATCH.code).build();
        }

        final var secret = decryptSplit[1];
        if(secret.isEmpty())
        {
            return ResponseEntity.status(SECRET_EMPTY.code).build();
        }

        try {
            timestampLastUpdate = Long.parseLong(decryptSplit[2]);
        } catch (final NumberFormatException e) {
            System.err.println(e.getMessage());
            return ResponseEntity.status(TIMESTAMP_LAST_NOT_PARSABLE.code).build();
        }

        optUser =  userRepository.findByEmailAndPasswd(decryptSplit[3], passwordEncoder.encode(decryptSplit[4]));
        if(optUser.isEmpty()) {
            return ResponseEntity.status(USER_NOT_FOUND.code).build();
        }

        cacheManager.add(new CacheRecord(
                uuid,
                secret,
                device,
                rsaHelper,
                now
        ));

        device.setTimestampLastLogin(now);
        deviceRepository.save(device);

        return ResponseEntity.ok(
                new Container(
                        now,
                        optUser.get(),
                        device,
                        groupController.getAll(uuid, timestampLastUpdate),
                        groupFieldController.getAll(uuid, timestampLastUpdate),
                        fieldController.getAll(uuid, timestampLastUpdate)
        ));
    }

    @PostMapping("/{uuid}/{crypt}")
    public @NotNull ResponseEntity<Container> persist(@NotNull final String uuid,
                                                      @NotNull final String crypt,
                                                      @NotNull final Container container
    ) throws CommonsException  {
        final var now = Instant.now(Clock.systemUTC()).getEpochSecond();

        long timestampLastUpdate = 0;
        Optional<User> optUser = Optional.empty();
        Device device = null;
        if(cacheManager.has(uuid)) {
            final var cacheRecord = cacheManager.get(uuid);
            if(cacheRecord.isPresent()) {
                var record = cacheRecord.get();
                device = record.getDevice();
                final var rsaHelper = record.getRsaHelper();

                final var decryptSplit = rsaHelper.decryptFromURLBase64(crypt).split("["+DIVISOR.value+"]");
                if(decryptSplit.length != 5)
                {
                    cacheManager.rm(record);
                    return ResponseEntity.status(WRONG_SIZE_TOKEN.code).build();
                }

                if(Long.parseLong(decryptSplit[0]) != device.getId())
                {
                    cacheManager.rm(record);
                    return ResponseEntity.status(DEVICE_ID_NOT_MATCH.code).build();
                }

                if(!decryptSplit[1].equals(record.getSecret()))
                {
                    cacheManager.rm(record);
                    return ResponseEntity.status(SECRET_NOT_MATCH.code).build();
                }

                if(checkTimestampLastUpdate != null && checkTimestampLastUpdate) {
                    timestampLastUpdate = Long.parseLong(decryptSplit[2]);
                    if(timestampLastUpdate != record.getTimestampLastUpdate())
                    {
                        cacheManager.rm(record);
                        return ResponseEntity.status(TIMESTAMP_LAST_UPDATE_NOT_MATCH.code).build();
                    }
                }

                optUser =  userRepository.findByEmailAndPasswd(decryptSplit[3], passwordEncoder.encode(decryptSplit[4]));
                if(optUser.isEmpty()) {
                    return ResponseEntity.status(USER_NOT_FOUND.code).build();
                }

                record.setTimestampLastUpdate(now);
            }
        } else {
            return ResponseEntity.status(CACHE_NOT_FOND.code).build();
        }


        if(device == null) {
            return ResponseEntity.status(DEVICE_NOT_FOUND.code).build();
        }

        groupController.clean();

        final var groups = groupController.store(uuid, now, container.groups());
        final var groupFields = groupFieldController.store(uuid, now, container.groupFields());
        final var fields = fieldController.store(uuid, now, container.fields());

        final var groupsDeleted = groupController.delete(uuid, now, container.groups());
        final var groupFieldsDeleted = groupFieldController.delete(uuid, now, container.groupFields());
        final var fieldsDeleted = fieldController.delete(uuid, now, container.fields());

        if(
                StreamSupport.stream(groups.spliterator(), false).findFirst().isPresent()
                || StreamSupport.stream(groupFields.spliterator(), false).findFirst().isPresent()
                || StreamSupport.stream(fields.spliterator(), false).findFirst().isPresent()
                || StreamSupport.stream(groupsDeleted.spliterator(), false).findFirst().isPresent()
                || StreamSupport.stream(groupFieldsDeleted.spliterator(), false).findFirst().isPresent()
                || StreamSupport.stream(fieldsDeleted.spliterator(), false).findFirst().isPresent()
        )
        {
            device.setTimestampLastUpdate(now);
            deviceRepository.save(device);


        }

        return ResponseEntity.ok(
                new Container(
                        now,
                        optUser.get(),
                        device,
                        concat(groups, groupsDeleted),
                        concat(groupFields, groupFieldsDeleted),
                        concat(fields, fieldsDeleted)
                ));
    }

    @PostMapping("/{uuid}/{crypt}")
    public @NotNull ResponseEntity<?> deleteCacheRecord(@NotNull final String uuid,
                                                        @NotNull final String crypt
    ) throws CommonsException {

        Optional<CacheRecord> cacheRecord;
        long timestampLastUpdate;
        Optional<User> optUser;
        Device device = null;
        if(cacheManager.has(uuid)) {
            cacheRecord = cacheManager.get(uuid);
            if(cacheRecord.isPresent()) {
                var record = cacheRecord.get();
                device = record.getDevice();
                final var rsaHelper = record.getRsaHelper();

                final var decryptSplit = rsaHelper.decryptFromURLBase64(crypt).split("["+DIVISOR.value+"]");
                if(decryptSplit.length != 5)
                {
                    cacheManager.rm(record);
                    return ResponseEntity.status(WRONG_SIZE_TOKEN.code).build();
                }

                if(Long.parseLong(decryptSplit[0]) != device.getId())
                {
                    cacheManager.rm(record);
                    return ResponseEntity.status(DEVICE_ID_NOT_MATCH.code).build();
                }

                if(!decryptSplit[1].equals(record.getSecret()))
                {
                    cacheManager.rm(record);
                    return ResponseEntity.status(SECRET_NOT_MATCH.code).build();
                }

                if(checkTimestampLastUpdate != null && checkTimestampLastUpdate) {
                    timestampLastUpdate = Long.parseLong(decryptSplit[2]);
                    if(timestampLastUpdate != record.getTimestampLastUpdate())
                    {
                        cacheManager.rm(record);
                        return ResponseEntity.status(TIMESTAMP_LAST_UPDATE_NOT_MATCH.code).build();
                    }
                }

                optUser =  userRepository.findByEmailAndPasswd(decryptSplit[3], passwordEncoder.encode(decryptSplit[4]));
                if(optUser.isEmpty()) {
                    return ResponseEntity.status(USER_NOT_FOUND.code).build();
                }

            }
        } else {
            return new ResponseEntity< Success<?> >(HttpStatus.OK);
        }


        if(device == null) {
            return ResponseEntity.status(DEVICE_NOT_FOUND.code).build();
        }

        if(cacheManager.rm(cacheRecord.get())) {
            return new ResponseEntity< Success<?> >(HttpStatus.OK);
        } else {
            return new ResponseEntity< Success<?> >(HttpStatus.NOT_FOUND);
        }

    }

    public static <T> @NotNull List<T> concat(Iterable<? extends T> a, Iterable<? extends T> b) {
        var merged = new LinkedList<T>();

        for (T item : a) {
            merged.add(item);
        }

        for (T item : b) {
            merged.add(item);
        }

        return merged;
    }

}
