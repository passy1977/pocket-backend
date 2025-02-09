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


package it.salsi.pocket;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@UtilityClass
public class Utils {

    @NotNull
    public <T> Collection<T> fromIterableToCollection(@NotNull final Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
    }

}
