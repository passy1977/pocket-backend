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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Log
@RestController
@RequestMapping("${server.api-version}/")
@Validated
public class SessionRest {

    private @NotNull final SessionController sessionController;

    public SessionRest(@Autowired @NotNull SessionController sessionController) {
        this.sessionController = sessionController;
    }

    @GetMapping("/{uuid}/{crypt}")
    public @NotNull ResponseEntity<Container> getData(
            @PathVariable 
            @NotBlank(message = "UUID cannot be blank")
            @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", 
                    message = "UUID must be in valid format")
            @NotNull final String uuid,
            @PathVariable 
            @NotBlank(message = "Crypt parameter cannot be blank")
            @Pattern(regexp = "^[A-Za-z0-9_-]{10,2048}={0,2}$", 
                    message = "Crypt parameter contains invalid characters or length")
            @NotNull final String crypt,
            @NotNull final HttpServletRequest request
    ) throws CommonsException
    {
        return sessionController.getData(uuid, crypt, SessionController.getClientIP(request));
    }

    @PostMapping("/{uuid}/{crypt}")
    public @NotNull ResponseEntity<Container> persist(
            @PathVariable 
            @NotBlank(message = "UUID cannot be blank")
            @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", 
                    message = "UUID must be in valid format")
            @NotNull final String uuid,
            @PathVariable 
            @NotBlank(message = "Crypt parameter cannot be blank")
            @Pattern(regexp = "^[A-Za-z0-9_-]{10,2048}={0,2}$", 
                    message = "Crypt parameter contains invalid characters or length")
            @NotNull final String crypt,
            @Valid @NotNull @RequestBody final Container container,
            @NotNull final HttpServletRequest request
    ) throws CommonsException
    {
        return sessionController.persist(uuid, crypt, container, SessionController.getClientIP(request));
    }

    @PutMapping("/{uuid}/{crypt}/{changePasswdDataOnServer}")
    public @NotNull ResponseEntity<Boolean> changePasswd(
            @PathVariable 
            @NotBlank(message = "UUID cannot be blank")
            @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", 
                    message = "UUID must be in valid format")
            @NotNull final String uuid,
            @PathVariable 
            @NotBlank(message = "Crypt parameter cannot be blank")
            @Pattern(regexp = "^[A-Za-z0-9_-]{10,2048}={0,2}$", 
                    message = "Crypt parameter contains invalid characters or length")
            @NotNull final String crypt,
            @PathVariable(required = false) @NotNull final Boolean changePasswdDataOnServer,
            @NotNull final HttpServletRequest request
    ) throws CommonsException
    {
        return sessionController.changePasswd(uuid, crypt, changePasswdDataOnServer, SessionController.getClientIP(request));
    }


    @DeleteMapping("/{uuid}/{crypt}")
    public @NotNull ResponseEntity<?> deleteCacheRecord(
            @PathVariable 
            @NotBlank(message = "UUID cannot be blank")
            @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", 
                    message = "UUID must be in valid format")
            @NotNull final String uuid,
            @PathVariable 
            @NotBlank(message = "Crypt parameter cannot be blank")
            @Pattern(regexp = "^[A-Za-z0-9_-]{10,2048}={0,2}$", 
                    message = "Crypt parameter contains invalid characters or length")
            @NotNull final String crypt
    ) throws CommonsException
    {
        return sessionController.deleteCacheRecord(uuid, crypt);
    }


    @GetMapping("/{uuid}/{crypt}/check")
    public @NotNull ResponseEntity<?> validateSession(
            @PathVariable 
            @NotBlank(message = "UUID cannot be blank")
            @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", 
                    message = "UUID must be in valid format")
            @NotNull final String uuid,
            @PathVariable 
            @NotBlank(message = "Crypt parameter cannot be blank")
            @Pattern(regexp = "^[A-Za-z0-9_-]{10,2048}={0,2}$", 
                    message = "Crypt parameter contains invalid characters or length")
            @NotNull final String crypt,
            @NotNull final HttpServletRequest request
    ) throws CommonsException
    {
        return sessionController.validateSession(uuid, crypt, SessionController.getClientIP(request));
    }
}
