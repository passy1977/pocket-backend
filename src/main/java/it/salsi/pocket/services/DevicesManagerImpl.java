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


import it.salsi.pocket.models.Device;
import it.salsi.pocket.repositories.DeviceRepository;
import it.salsi.pocket.repositories.PropertyRepository;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;

import static it.salsi.pocket.Constant.PROPERTY_INVALIDATOR_ENABLE;
import static it.salsi.pocket.Constant.PROPERTY_INVALIDATOR_MAX_LOGIN_DAYS;

@Log
@Service
public final class DevicesManagerImpl implements DevicesManager {

    @NotNull
    private final PropertyRepository propertyRepository;

    @NotNull
    private final DeviceRepository deviceRepository;


    public DevicesManagerImpl(@Autowired @NotNull PropertyRepository propertyRepository,
                              @Autowired @NotNull DeviceRepository deviceRepository) {
        this.propertyRepository = propertyRepository;
        this.deviceRepository = deviceRepository;
    }


    public void invalidateAll() {

        log.info("start invalidate");

        propertyRepository.getByKey(PROPERTY_INVALIDATOR_ENABLE).ifPresentOrElse(invalidatorEnable -> {
            if (Boolean.TRUE.toString().equals(invalidatorEnable.getValue())) {
                try {
                    propertyRepository.getByKey(PROPERTY_INVALIDATOR_MAX_LOGIN_DAYS).ifPresentOrElse(invalidatorMaxLoginDays -> {
                        log.info("start invalidator thread: " + Thread.currentThread().getName());

                        try {
                            final var maxLoginDays = Integer.parseInt(invalidatorMaxLoginDays.getValue());

                            for (final var device : deviceRepository.findAll()) {

                                if (ChronoUnit.DAYS.between(device.getDateTimeLastUpdate().toInstant(), device.getUser().getDateTimeLastUpdate().toInstant()) > maxLoginDays) {
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
