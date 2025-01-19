package it.salsi.pocket.services;

import it.salsi.pocket.models.Device;
import it.salsi.pocket.security.RSAHelper;
import org.jetbrains.annotations.NotNull;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

public interface CacheManager {

    record CacheRecord(
            @NotNull String uuid,
            @NotNull Device device,
            @NotNull RSAHelper rsaHelper,
            long timestampCreation
    ) {}

    boolean add(@NotNull final CacheRecord record);

    @NotNull Optional<CacheRecord> get(@NotNull final CacheRecord record);

    boolean rm(@NotNull final CacheRecord record);

    boolean has(@NotNull final CacheRecord record);

}
