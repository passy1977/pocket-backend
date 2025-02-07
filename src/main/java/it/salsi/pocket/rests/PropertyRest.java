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
