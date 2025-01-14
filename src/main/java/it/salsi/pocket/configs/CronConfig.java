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

package it.salsi.pocket.configs;


import it.salsi.commons.CommonsException;
import it.salsi.pocket.services.DatabaseManager;
import it.salsi.pocket.services.DevicesManager;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;


@Slf4j
@Configuration
@EnableAsync
@EnableScheduling
public class CronConfig {

    @NotNull
    private final DatabaseManager databaseManager;

    @NotNull
    private final DevicesManager devicesManager;

    public CronConfig(
            @Autowired @NotNull final DatabaseManager databaseManager,
            @Autowired @NotNull final DevicesManager devicesManager
    ) {
        this.databaseManager = databaseManager;
        this.devicesManager = devicesManager;
    }


    @Scheduled(cron = "55 23 * * * ?")
    final public void authTokenExpiration() {
        try {
            devicesManager.invalidateAll();
            databaseManager.cleanOldData();
        } catch (CommonsException e) {
            log.error(e.getMessage());
        }

    }
}
