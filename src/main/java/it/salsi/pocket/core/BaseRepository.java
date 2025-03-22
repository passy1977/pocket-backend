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


package it.salsi.pocket.core;

import it.salsi.pocket.models.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface BaseRepository<T> extends CrudRepository<T, Long> {

//    Optional<T> findByUserAndId(@NotNull final User userId, @NotNull final Long id);

    Iterable<T> findByUser(@NotNull final User userId);

    Iterable<T> findByUserAndTimestampLastUpdateGreaterThan(@NotNull final User userId, @NotNull final Long dateTimeLastUpdate);

    Iterable<T> findByUserAndDeletedAndTimestampLastUpdateLessThan(@NotNull final User userId, boolean deleted, @NotNull final Long dateTimeLastUpdate);
}
