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

package it.salsi.pocket;

import it.salsi.commons.CommonsException;
import it.salsi.pocket.services.*;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Log
@Component
public record ApplicationStartup(@NotNull DatabaseManager databaseManager,
                                 @NotNull DevicesManager devicesManager,
                                 @NotNull PropertiesManager propertiesManager,
                                 @NotNull UserManager userManager) implements ApplicationListener<ApplicationReadyEvent> {

    public ApplicationStartup(@Autowired @NotNull final DatabaseManager databaseManager,
                              @Autowired @NotNull final DevicesManager devicesManager,
                              @Autowired @NotNull final PropertiesManager propertiesManager,
                              @NotNull UserManager userManager) {
        this.databaseManager = databaseManager;
        this.devicesManager = devicesManager;
        this.propertiesManager = propertiesManager;
        this.userManager = userManager;
    }


    @Override
    public void onApplicationEvent(@NotNull final ApplicationReadyEvent applicationReadyEvent) {
        try {
            databaseManager.init();
            propertiesManager.checkAll();
        } catch (CommonsException e) {
            log.warning(e.getLocalizedMessage());
        }
        userManager.checkAll();
    }
}
