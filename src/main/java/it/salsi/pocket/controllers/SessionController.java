package it.salsi.pocket.controllers;

import it.salsi.commons.CommonsException;
import it.salsi.commons.utils.Crypto;
import it.salsi.pocket.models.*;
import it.salsi.pocket.repositories.DeviceRepository;
import it.salsi.pocket.repositories.UserRepository;
import it.salsi.pocket.security.PasswordEncoder;
import it.salsi.pocket.security.RSAHelper;
import it.salsi.pocket.services.CacheManager;
import it.salsi.pocket.services.CacheManager.CacheRecord;
import jakarta.validation.Valid;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.Clock;
import java.time.Instant;
import java.util.*;

import static it.salsi.pocket.controllers.SessionController.ErrorCode.*;
import static it.salsi.pocket.Constant.DIVISOR;
import static it.salsi.pocket.security.RSAHelper.*;

@Log
@Service
public record SessionController(
        @Autowired @NotNull UserRepository userRepository,
        @Autowired @NotNull DeviceRepository deviceRepository,
        @Autowired @NotNull GroupController groupController,
        @Autowired @NotNull GroupFieldController groupFieldController,
        @Autowired @NotNull FieldController fieldController,
        @Autowired @NotNull Crypto crypto,
        @Autowired @NotNull PasswordEncoder passwordEncoder,
        @Autowired @NotNull RSAHelper rsaHelper,
        @Autowired @NotNull CacheManager cacheManager
        ) {

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
        FOO(0);

        ErrorCode(int code) {
            this.code = code;
        }

        public final int code;
    }

    public @NotNull ResponseEntity<Container> getData(@NotNull final String uuid,
                                                      @NotNull final String crypt,
                                                      @NotNull final String email,
                                                      @NotNull final String passwd) throws CommonsException {


        final var optUser = userRepository.findByEmailAndPasswd(email, passwd);
        if(optUser.isEmpty()) {
            return ResponseEntity.status(USER_NOT_FOUND.code).build();
        }

        final var now = Instant.now(Clock.systemUTC()).getEpochSecond();

        long timestampLastUpdate = 0;
        Device device = null;
        RSAHelper rsaHelper = null;
        if(cacheManager.has(uuid)) {
            final var cacheRecord = cacheManager.get(uuid);
            if(cacheRecord.isPresent()) {
                var record = cacheRecord.get();
                device = record.getDevice();
                rsaHelper = record.getRsaHelper();


                final var decryptSplit = rsaHelper.decryptFromURLBase64(crypt).split("["+DIVISOR.value+"]");
                if(decryptSplit.length != 3)
                {
                    cacheManager.rm(record);
                    return ResponseEntity.status(WRONG_SIZE_TOKEN.code).build();
                }

                if(Long.parseLong(decryptSplit[0]) != device.getId())
                {
                    cacheManager.rm(record);
                    return ResponseEntity.status(WRONG_SIZE_TOKEN.code).build();
                }

                if(!decryptSplit[1].equals(record.getSecret()))
                {
                    cacheManager.rm(record);
                    return ResponseEntity.status(SECRET_NOT_MATCH.code).build();
                }

                timestampLastUpdate = Long.parseLong(decryptSplit[2]);
                if(timestampLastUpdate != record.getTimestampLastUpdate())
                {
                    cacheManager.rm(record);
                    return ResponseEntity.status(TIMESTAMP_LAST_UPDATE_NOT_MATCH.code).build();
                }

            }
        } else {
            final var optDevice = deviceRepository.findByUuid(uuid);
            if(optDevice.isEmpty()) {
                return ResponseEntity.status(DEVICE_NOT_FOUND.code).build();
            }

            device = optDevice.get();
            rsaHelper = new RSAHelper(ALGORITHM, KEY_SIZE);
            rsaHelper.loadPublicKey(Base64.getDecoder().decode(device.getPublicKey()));
            rsaHelper.loadPrivateKey(Base64.getDecoder().decode(device.getPrivateKey()));

            final var decryptSplit = rsaHelper.decryptFromURLBase64(crypt).split("["+DIVISOR.value+"]");
            if(decryptSplit.length != 3)
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

            cacheManager.add(new CacheRecord(
                    uuid,
                    secret,
                    device,
                    rsaHelper,
                    now
            ));
        }
        if(device == null) {
            return ResponseEntity.status(DEVICE_NOT_FOUND.code).build();
        }

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
    public @NotNull ResponseEntity<Container> setData(@PathVariable @NotNull final String uuid,
                                                      @PathVariable @NotNull final String crypt,
                                                      @Valid @NotNull @RequestBody final List<Container> list
    ) throws CommonsException  {
        final var now = Instant.now(Clock.systemUTC()).getEpochSecond();

        long timestampLastUpdate = 0;
        Device device = null;
        RSAHelper rsaHelper = null;
        if(cacheManager.has(uuid)) {
            final var cacheRecord = cacheManager.get(uuid);
            if(cacheRecord.isPresent()) {
                var record = cacheRecord.get();
                device = record.getDevice();
                rsaHelper = record.getRsaHelper();

                final var decryptSplit = rsaHelper.decryptFromURLBase64(crypt).split("["+DIVISOR.value+"]");
                if(decryptSplit.length != 4)
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

                timestampLastUpdate = Long.parseLong(decryptSplit[2]);
                if(timestampLastUpdate != record.getTimestampLastUpdate())
                {
                    cacheManager.rm(record);
                    return ResponseEntity.status(TIMESTAMP_LAST_UPDATE_NOT_MATCH.code).build();
                }

                if(Long.parseLong(decryptSplit[3]) != device.getUser().getId())
                {
                    cacheManager.rm(record);
                    return ResponseEntity.status(USER_ID_NOT_MATCH.code).build();
                }

            }
        } else {
            return ResponseEntity.status(CACHE_NOT_FOND.code).build();
        }






        if(device == null) {
            return ResponseEntity.status(DEVICE_NOT_FOUND.code).build();
        }

        device.setTimestampLastLogin(now);
        deviceRepository.save(device);

        return ResponseEntity.ok(
                new Container(
                        now,
                        null,
                        null,
                        groupController.getAll(uuid, timestampLastUpdate),
                        groupFieldController.getAll(uuid, timestampLastUpdate),
                        fieldController.getAll(uuid, timestampLastUpdate)
                ));
    }

}
