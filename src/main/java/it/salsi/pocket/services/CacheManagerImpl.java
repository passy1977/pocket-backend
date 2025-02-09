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

import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Log
@Service
public final class CacheManagerImpl implements CacheManager {

    @NotNull
    private final Map<String, CacheRecord> map = new HashMap<>();

    public boolean add(@NotNull final CacheRecord record) {
        if(map.containsKey(record.getUuid())) {
            return false;
        }
        map.put(record.getUuid(), record);
        return true;
    }

    public @NotNull Optional<CacheRecord> get(@NotNull final CacheRecord record) {
        return get(record.getUuid());
    }

    public @NotNull Optional<CacheRecord> get(@NotNull final String uuid) {
        if(!map.containsKey(uuid)) {
            return Optional.empty();
        }
        return Optional.ofNullable(map.get(uuid));
    }

    public boolean rm(@NotNull final CacheRecord record) {
        if(!map.containsKey(record.getUuid())) {
            return false;
        }
        return map.remove(record.getUuid()) != null;
    }

    public boolean has(@NotNull final CacheRecord record) {
        return has(record.getUuid());
    }

    public boolean has(@NotNull final String uuid) {
        return map.containsKey(uuid);
    }

}
