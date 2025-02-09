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

import it.salsi.pocket.ResponseEntityUtils;
import it.salsi.pocket.models.Device;
import it.salsi.pocket.models.Property;
import it.salsi.pocket.models.User;
import it.salsi.pocket.repositories.DeviceRepository;
import it.salsi.pocket.repositories.PropertyRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Deprecated
@RestController
@RequestMapping("${server.api-version}/property/")
public record PropertyRest(@NotNull PropertyRepository repository,
                           @NotNull DeviceRepository deviceRepository) {

    @GetMapping("/{token}")
    public ResponseEntity<Iterable<Property>> list(@PathVariable @NotNull final String token) {

        final var device = deviceRepository.findByUuid(token);
        if (device.isPresent()) {
            if (device.get().getStatus() != Device.Status.ACTIVE)
                return ResponseEntityUtils.returnNonAuthoritativeInformation(List.of());
            if (device.get().getUser().getStatus() != User.Status.ACTIVE)
                return ResponseEntityUtils.returnNonAuthoritativeInformation(List.of());

            final List<Property> ret = new ArrayList<>();

            for (final var it : repository.findAll()) {
                if (it.getMetaProperty().toApp()) {
                    ret.add(it);
                }
            }

            return ResponseEntity.ok(ret);
        } else return ResponseEntityUtils.returnNoContent(List.of());
    }

}
