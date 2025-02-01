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

import java.time.Clock;
import java.time.Instant;
import java.util.*;

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

    public @NotNull ResponseEntity<Container> getData(@NotNull final String uuid,
                                                      @NotNull final String crypt,
                                                      @NotNull final Long timestampLastUpdate,
                                                      @NotNull final String email,
                                                      @NotNull final String passwd) throws CommonsException {


        final var optUser = userRepository.findByEmailAndPasswd(email, passwd);
        if(optUser.isEmpty()) {
            return ResponseEntity.status(600).build();
        }

        final var now = Instant.now(Clock.systemUTC()).getEpochSecond();

        Device device = null;
        RSAHelper rsaHelper = null;
        if(cacheManager.has(uuid)) {
            final var cacheRecord = cacheManager.get(uuid);
            if(cacheRecord.isPresent()) {
                var record = cacheRecord.get();
                device = record.getDevice();
                rsaHelper = record.getRsaHelper();


                final var decryptSplit = rsaHelper.decryptFromURLBase64(crypt).split("["+DIVISOR.value+"]");
                if(decryptSplit.length != 2)
                {
                    return ResponseEntity.status(601).build();
                }

                if(Integer.parseInt(decryptSplit[0]) != device.getId())
                {
                    return ResponseEntity.status(602).build();
                }

                if(!decryptSplit[1].equals(record.getSecret()))
                {
                    cacheManager.rm(record);
                    return ResponseEntity.status(605).build();
                }

            }
        } else {
            final var optDevice = deviceRepository.findByUuid(uuid);
            if(optDevice.isEmpty()) {
                return ResponseEntity.status(604).build();
            }

            device = optDevice.get();
            rsaHelper = new RSAHelper(ALGORITHM, KEY_SIZE);
            rsaHelper.loadPublicKey(Base64.getDecoder().decode(device.getPublicKey()));
            rsaHelper.loadPrivateKey(Base64.getDecoder().decode(device.getPrivateKey()));

            final var decryptSplit = rsaHelper.decryptFromURLBase64(crypt).split("["+DIVISOR.value+"]");
            if(decryptSplit.length != 2)
            {
                return ResponseEntity.status(601).build();
            }

            if(Integer.parseInt(decryptSplit[0]) != device.getId())
            {
                return ResponseEntity.status(602).build();
            }

            cacheManager.add(new CacheRecord(
                    uuid,
                    decryptSplit[1],
                    device,
                    rsaHelper,
                    now
            ));
        }
        if(device == null) {
            return ResponseEntity.status(603).build();
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

}
