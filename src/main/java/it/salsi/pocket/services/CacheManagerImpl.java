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

import it.salsi.pocket.models.User;
import it.salsi.pocket.repositories.PropertyRepository;
import it.salsi.pocket.repositories.UserRepository;
import it.salsi.pocket.security.PasswordEncoder;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static it.salsi.pocket.Constant.*;
import static java.time.Instant.ofEpochSecond;

@Log
@Service
public final class CacheManagerImpl implements CacheManager {

    @NotNull
    private final PropertyRepository propertyRepository;

    @NotNull
    private final UserRepository userRepository;

    @NotNull
    private final PasswordEncoder passwordEncoder;

    @Value("${server.auth.user}")
    @Nullable
    private String authUser;

    @Value("${server.auth.passwd}")
    @Nullable
    private String authPasswd;

    @NotNull
    private final Map<String, CacheRecord> map = new HashMap<>();

    public CacheManagerImpl(@Autowired @NotNull final PropertyRepository propertyRepository
            , @Autowired @NotNull final UserRepository userRepository
            , @Autowired @NotNull final PasswordEncoder passwordEncoder) {
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean add(@NotNull final CacheRecord record) {
        if(map.containsKey(record.getUuid())) {
            return false;
        }
        map.put(record.getUuid(), record);
        return true;
    }

    @Override
    public @NotNull Optional<CacheRecord> get(@NotNull final CacheRecord record) {
        return get(record.getUuid());
    }

    @Override
    public @NotNull Optional<CacheRecord> get(@NotNull final String uuid) {
        if(!map.containsKey(uuid)) {
            return Optional.empty();
        }
        return Optional.ofNullable(map.get(uuid));
    }

    @Override
    public boolean rm(@NotNull String uuid) {
        if(!map.containsKey(uuid)) {
            return false;
        }
        return map.remove(uuid) != null;
    }

    @Override
    public boolean rm(@NotNull final CacheRecord record) {
        return rm(record.getUuid());
    }

    @Override
    public boolean has(@NotNull final CacheRecord record) {
        return has(record.getUuid());
    }

    @Override
    public boolean has(@NotNull final String uuid) {
        return map.containsKey(uuid);
    }

    @Override
    public void invalidate() {
        log.info("Start invalidate");

        AtomicReference<User> adminUser = new AtomicReference<>(new User());
        userRepository.findByEmail(authUser).ifPresentOrElse(
                adminUser::set,
                () -> adminUser.set(userRepository.save(new User(authUser, authUser, passwordEncoder.encode(authPasswd))))
        );

        propertyRepository.getByUserIdAndKey(adminUser.get().getId(), PROPERTY_INVALIDATOR_ENABLE).ifPresentOrElse(invalidatorEnable -> {
            if (Boolean.TRUE.toString().equals(invalidatorEnable.getValue())) {
                try {
                    propertyRepository.getByUserIdAndKey(adminUser.get().getId(), PROPERTY_INVALIDATOR_CACHE_MAX_MINUTES).ifPresentOrElse(invalidatorCacheMaxMinutes -> {
                        log.info("Start invalidator thread: " + Thread.currentThread().getName());

                        final var now = Instant.now(Clock.systemUTC()).getEpochSecond();

                        for(final var key : map.keySet()) {
                            
                            final var maxMinutes = Integer.parseInt(invalidatorCacheMaxMinutes.getValue());
                            if (ChronoUnit.MINUTES.between(ofEpochSecond(map.get(key).getTimestampLastUpdate()), ofEpochSecond(now)) > maxMinutes) {
                                log.info("Invalidate: " + key);
                                map.remove(key);
                            }
                        }


                    }, () -> log.severe("cron invalidator not stared: invalid conversion date"));


                } catch (NumberFormatException e) {
                    log.severe("cron invalidator disabled");
                }

            } else {
                log.warning("cron invalidator not stared: invalid date");
            }

        }, () -> log.warning("cron invalidator disabled"));


    }
}
