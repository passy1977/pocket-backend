package it.salsi.pocket.core;

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

import it.salsi.pocket.models.Device;
import it.salsi.pocket.models.User;
import it.salsi.pocket.repositories.DeviceRepository;
import it.salsi.pocket.repositories.UserRepository;
import lombok.Setter;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Log
public class BaseController <T extends BaseModel, Y extends BaseRepository<T>> {

    @FunctionalInterface
    protected interface OnStore<T> {
        @NotNull
        T perform(@NotNull final T t);
    }

    @NotNull
    final private Y repository;

    @NotNull
    final private UserRepository userRepository;

    @NotNull
    final private DeviceRepository deviceRepository;

    @Setter
    @Nullable
    private BaseController.OnStore<T> onStore;

    public BaseController(@Autowired @NotNull final Y repository,
                    @Autowired @NotNull final DeviceRepository deviceRepository,
                    @Autowired @NotNull final UserRepository userRepository
    ) {
        this.repository = repository;
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
    }


    @NotNull
    public Iterable<T> getAll(@NotNull final String token,
                              @NotNull final Long timestampLastUpdate
    ) {
        final var device = deviceRepository.findByUuid(token);
        if (device.isPresent()) {
            if (device.get().getStatus() != Device.Status.ACTIVE) return List.of();
            if (device.get().getUser().getStatus() != User.Status.ACTIVE) return List.of();
            final var ret = repository.findByUserAndTimestampLastUpdateGreaterThan(device.get().getUser(), timestampLastUpdate);
            ret.forEach(T::switchId);
            return ret;
        } else return List.of();
    }

    @NotNull
    public Iterable<T> store(@NotNull final String uuid
            , @NotNull final Long now
            , @Nullable final Iterable<T> elements
    ) {

        if(elements == null) {
            return List.of();
        }

        List<T> ret = new ArrayList<>();

        final var device = deviceRepository.findByUuid(uuid);
        if (device.isPresent()) {
            if (device.get().getStatus() != Device.Status.ACTIVE) return List.of();
            if (device.get().getUser().getStatus() != User.Status.ACTIVE) return List.of();

            for(final var it : elements) {
                if(!it.deleted) {
                    it.setUser(device.get().getUser());
                    it.setTimestampLastUpdate(now);

                    final var tmp = it.id;
                    it.id = it.serverId;
                    it.serverId = tmp;

                    try {

                        var original = new AtomicReference<T>();
                        Optional.ofNullable(onStore).ifPresent(onStore -> original.set(onStore.perform(it)));

                        @SuppressWarnings("unchecked") final var base = (T) repository.save(original.get()).clone();
                        base.postStore(original.get());

                        ret.add(base);
                    } catch (CloneNotSupportedException e) {
                        throw new RuntimeException(e);
                    }
                }
                else
                {
                    //todo: handle deleted
                }
            }
        }

        if (!ret.isEmpty()) {
            ret.forEach(BaseModel::switchId);
        }

        return ret;
    }

}
