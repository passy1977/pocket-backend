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
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.*;

import static it.salsi.pocket.Constant.DIVISOR;

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

    public @NotNull ResponseEntity<Container> getFullData(@NotNull final String uuid,
                                                          @NotNull final Long timestampLastUpdate,
                                                          @NotNull final String email,
                                                          @NotNull final String passwd) throws CommonsException {


        final var optUser = userRepository.findByEmailAndPasswd(email, passwd);
        if(optUser.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        final var now = Instant.now(Clock.systemUTC()).getEpochSecond();

        Device device = null;
        RSAHelper rsaHelper = null;
        String newPasswd = null;
        if(cacheManager.has(uuid)) {
            var cacheRecord = cacheManager.get(uuid);
            if(cacheRecord.isPresent()) {
                device = cacheRecord.get().device();
                rsaHelper = cacheRecord.get().rsaHelper();
                newPasswd = cacheRecord.get().passwd();
            }
        } else {
            var optDevice = deviceRepository.findByUuid(uuid);
            if(optDevice.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            device = optDevice.get();
            rsaHelper = new RSAHelper(RSAHelper.ALGORITHM, RSAHelper.KEY_SIZE);
            rsaHelper.loadPrivateKey(Base64.getDecoder().decode(device.getPrivateKey()));
            rsaHelper.loadPublicKey(Base64.getDecoder().decode(device.getPublicKey()));

            byte[] array = new byte[32];
            new Random().nextBytes(array);
            newPasswd = new String(array, StandardCharsets.UTF_8);
            cacheManager.add(new CacheRecord(
                    uuid,
                    newPasswd,
                    device,
                    rsaHelper,
                    now
            ));
        }
        assert device != null;



        device.setTimestampLastLogin(now);
        deviceRepository.save(device);

        //user_id|device_uuid|pwd|random
        final var token = (device.getUser().getId() + DIVISOR.value + device.getUuid() + DIVISOR.value + newPasswd + DIVISOR.value + UUID.randomUUID()).getBytes(StandardCharsets.UTF_8);

        

        return ResponseEntity.ok(
                new Container(
                        rsaHelper.encryptToString(token),
                        optUser.get(),
                        device,
                        groupController.getAll(uuid, timestampLastUpdate),
                        groupFieldController.getAll(uuid, timestampLastUpdate),
                        fieldController.getAll(uuid, timestampLastUpdate)
        ));
    }

}
