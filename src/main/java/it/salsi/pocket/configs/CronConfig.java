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


package it.salsi.pocket.configs;


import it.salsi.commons.CommonsException;
import it.salsi.pocket.services.DatabaseManager;
import it.salsi.pocket.services.DevicesManager;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;


@Log
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


    @Scheduled(cron = "${server.services-cron}")
    final public void servicesCron() {
        try {
            devicesManager.invalidateAll();
            databaseManager.cleanOldData();
        } catch (CommonsException e) {
            log.severe(e.getMessage());
        }

    }
}
