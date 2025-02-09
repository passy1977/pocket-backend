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

import it.salsi.pocket.Constant;
import it.salsi.pocket.models.Property;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
public interface PropertyRepository extends CrudRepository<Property, Long> {

    @NotNull
    default Optional<Property> getByUserIdAndKey(@NotNull final Long userId, @NotNull final Constant key) {
        return getByUserIdAndKey(userId, key.value);
    }

    @NotNull
    Optional<Property> getByUserIdAndKey(@NotNull final Long userId, @NotNull final String key);

    @NotNull
    List<Property> findAllByOrderByKey();

}
