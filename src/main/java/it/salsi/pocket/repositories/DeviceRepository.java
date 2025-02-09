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


package it.salsi.pocket.repositories;

import it.salsi.pocket.models.Device;
import it.salsi.pocket.models.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;


public interface DeviceRepository extends CrudRepository<Device, Long> {

    //TODO:Handle this situation
    Optional<Device> findByUuid(@NotNull final String uuid);

    Optional<Device> findByUserAndUuid(@NotNull final User user, @NotNull final String uuid);

    @SuppressWarnings("UnusedReturnValue")
            //TODO:Handle this situation
//    @Transactional
//    Long deleteByToken(@NotNull final String token);

    Long countAllByUserAndTimestampLastUpdateBeforeAndStatusIsNot(@NotNull final User user, @NotNull final Long beforeDate, @NotNull final Device.Status statusNotIn);

}
