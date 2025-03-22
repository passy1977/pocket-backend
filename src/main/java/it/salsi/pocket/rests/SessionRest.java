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

package it.salsi.pocket.rests;

import it.salsi.commons.CommonsException;
import it.salsi.pocket.controllers.SessionController;
import it.salsi.pocket.models.Container;
import jakarta.validation.Valid;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Log
@RestController
@RequestMapping("${server.api-version}/")
public class SessionRest {

    private @NotNull final SessionController sessionController;

    public SessionRest(@Autowired @NotNull SessionController sessionController) {
        this.sessionController = sessionController;
    }

    @GetMapping("/{uuid}/{crypt}")
    public @NotNull ResponseEntity<Container> getData(@PathVariable @NotNull final String uuid,
                                                          @PathVariable @NotNull final String crypt) throws CommonsException
    {
        return sessionController.getData(uuid, crypt);
    }

    @PostMapping("/{uuid}/{crypt}")
    public @NotNull ResponseEntity<Container> persist(@PathVariable @NotNull final String uuid,
                                                          @PathVariable @NotNull final String crypt,
                                                          @Valid @NotNull @RequestBody final Container container
                                                        ) throws CommonsException
    {
        return sessionController.persist(uuid, crypt, container);
    }

    @PutMapping("/{uuid}/{crypt}")
    public @NotNull ResponseEntity<Boolean> changePasswd(@PathVariable @NotNull final String uuid,
                                                      @PathVariable @NotNull final String crypt
    ) throws CommonsException
    {
        return sessionController.changePasswd(uuid, crypt);
    }


    @DeleteMapping("/{uuid}/{crypt}")
    public @NotNull ResponseEntity<?> deleteCacheRecord(@PathVariable @NotNull final String uuid,
                                                      @PathVariable @NotNull final String crypt
    ) throws CommonsException
    {
        return sessionController.deleteCacheRecord(uuid, crypt);
    }


}
