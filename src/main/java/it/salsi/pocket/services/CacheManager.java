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

package it.salsi.pocket.services;

import it.salsi.pocket.models.Device;
import it.salsi.pocket.security.RSAHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface CacheManager {

    @Getter
    @Setter
    @RequiredArgsConstructor
    final class CacheRecord {

        @NotNull String uuid;
        @NotNull String secret;
        @NotNull Device device;
        @NotNull RSAHelper rsaHelper;
        long timestampLastUpdate;

        public CacheRecord(
                @NotNull String uuid,
                @NotNull String secret,
                @NotNull Device device,
                @NotNull RSAHelper rsaHelper,
                long timestampLastUpdate
        ) {
            this.uuid = uuid;
            this.secret = secret;
            this.device = device;
            this.rsaHelper = rsaHelper;
            this.timestampLastUpdate = timestampLastUpdate;
        }

    }

    boolean add(@NotNull final CacheRecord record);

    @NotNull Optional<CacheRecord> get(@NotNull final CacheRecord record);

    @NotNull Optional<CacheRecord> get(@NotNull final String uuid);

    boolean rm(@NotNull final CacheRecord record);

    boolean has(@NotNull final CacheRecord record);

    boolean has(@NotNull final String uuid);

}
