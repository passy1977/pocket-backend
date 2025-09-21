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


package it.salsi.pocket;

import it.salsi.commons.CommonsException;
import it.salsi.pocket.services.*;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;


@Log
@Component
public record ApplicationStartup(@NotNull DatabaseManager databaseManager,
                                 @NotNull PropertiesManager propertiesManager,
                                 @NotNull UserManager userManager,
                                 @NotNull IpcSocketManager ipcSocketManager) implements ApplicationListener<ApplicationReadyEvent> {

    public ApplicationStartup(@Autowired @NotNull final DatabaseManager databaseManager,
                              @Autowired @NotNull final PropertiesManager propertiesManager,
                              @Autowired @NotNull final UserManager userManager,
                              @Autowired @NotNull final IpcSocketManager ipcSocketManager
    ) {
        this.databaseManager = databaseManager;
        this.propertiesManager = propertiesManager;
        this.userManager = userManager;
        this.ipcSocketManager = ipcSocketManager;
    }


    @Override
    public void onApplicationEvent(@NotNull final ApplicationReadyEvent applicationReadyEvent) {
        try {
            userManager.checkAll();
            databaseManager.init();
            propertiesManager.checkAll();
            ipcSocketManager.start();
        } catch (CommonsException e) {
            log.warning(e.getLocalizedMessage());
        }
    }

    @EventListener
    public void handleContextRefresh(final @NotNull ContextRefreshedEvent event) {
        final var applicationContext = event.getApplicationContext();
        final var requestMappingHandlerMapping = applicationContext
                .getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        final var map = requestMappingHandlerMapping
                .getHandlerMethods();
        map.forEach((key, value) -> log.info(key + " "+ value));
    }
}
