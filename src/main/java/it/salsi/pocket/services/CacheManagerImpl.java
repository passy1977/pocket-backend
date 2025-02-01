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
