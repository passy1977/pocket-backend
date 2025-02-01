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
        long timestampCreation;

        public CacheRecord(
                @NotNull String uuid,
                @NotNull String secret,
                @NotNull Device device,
                @NotNull RSAHelper rsaHelper,
                long timestampCreation
        ) {
            this.uuid = uuid;
            this.secret = secret;
            this.device = device;
            this.rsaHelper = rsaHelper;
            this.timestampCreation = timestampCreation;
        }

    }

    boolean add(@NotNull final CacheRecord record);

    @NotNull Optional<CacheRecord> get(@NotNull final CacheRecord record);

    @NotNull Optional<CacheRecord> get(@NotNull final String uuid);

    boolean rm(@NotNull final CacheRecord record);

    boolean has(@NotNull final CacheRecord record);

    boolean has(@NotNull final String uuid);

}
