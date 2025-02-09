package it.salsi.pocket.core;

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

import it.salsi.pocket.models.Device;
import it.salsi.pocket.models.User;
import it.salsi.pocket.repositories.DeviceRepository;
import it.salsi.pocket.repositories.UserRepository;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Log
public class BaseController <T extends BaseModel, Y extends BaseRepository<T>> {

    @NotNull
    final private Y repository;

    @NotNull
    final private UserRepository userRepository;

    @NotNull
    final private DeviceRepository deviceRepository;

    public BaseController(@Autowired @NotNull final Y repository,
                    @Autowired @NotNull final DeviceRepository deviceRepository,
                    @Autowired @NotNull final UserRepository userRepository
    ) {
        this.repository = repository;
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
    }


    public Iterable<T> getAll(@NotNull final String token,
                              @NotNull final Long timestampLastUpdate
    ) {
        final var device = deviceRepository.findByUuid(token);
        if (device.isPresent()) {
            if (device.get().getStatus() != Device.Status.ACTIVE) return List.of();
            if (device.get().getUser().getStatus() != User.Status.ACTIVE) return List.of();
            final var ret = repository.findByUserAndTimestampLastUpdateGreaterThan(device.get().getUser(), timestampLastUpdate);
            ret.forEach(T::switchId);
            return ret;
        } else return List.of();
    }

}
