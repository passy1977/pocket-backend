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

package it.salsi.pocket.models;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Container(
        @NotNull Long timestampLastUpdate,
        @Nullable User user,
        @Nullable Device device,
        @NotNull Iterable<Group> groups,
        @NotNull Iterable<GroupField> groupsFields,
        @NotNull Iterable<Field> fields
) {}
