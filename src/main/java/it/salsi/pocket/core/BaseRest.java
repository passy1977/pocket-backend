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

import it.salsi.pocket.Constant;
import it.salsi.pocket.ResponseEntityUtils;
import it.salsi.pocket.models.Device;
import it.salsi.pocket.models.User;
import it.salsi.pocket.repositories.DeviceRepository;
import it.salsi.pocket.repositories.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.Setter;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Deprecated
@SuppressWarnings("Duplicates")
@Log
public class BaseRest<T extends BaseModel, Y extends BaseRepository<T>> {

    @FunctionalInterface
    protected interface OnSave<T> {
        @NotNull
        T perform(@NotNull final T t);
    }

    @FunctionalInterface
    protected interface OnReturn<T> {
        void perform(@NotNull final T original, @NotNull final T ret);
    }

    @NotNull
    final private Y repository;

    @NotNull
    final private UserRepository userRepository;

    @NotNull
    final private DeviceRepository deviceRepository;

    @Setter
    @Nullable
    private OnSave<T> onSave;

    @Setter
    @Nullable
    private OnReturn<T> onReturn;

    public BaseRest(@Autowired @NotNull final Y repository,
                    @Autowired @NotNull final DeviceRepository deviceRepository,
                    @Autowired @NotNull final UserRepository userRepository
    ) {
        this.repository = repository;
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
    }


    @GetMapping("/{token}")
    public ResponseEntity<Iterable<T>> getAll(@PathVariable @NotNull final String token) {

        final var device = deviceRepository.findByUuid(token);
        if (device.isPresent()) {
            if (device.get().getStatus() != Device.Status.ACTIVE) return ResponseEntityUtils.returnNonAuthoritativeInformation(List.of());
            if (device.get().getUser().getStatus() != User.Status.ACTIVE) return ResponseEntityUtils.returnNonAuthoritativeInformation(List.of());
            final var ret = repository.findByUser(device.get().getUser());
            ret.forEach(it -> it.setServerId(it.getId()));
            return ResponseEntity.ok(ret);
        } else return ResponseEntityUtils.returnNoContent(List.of());
    }

    @GetMapping("/{token}/{dateTimeLastUpdate}")
    public ResponseEntity<Iterable<T>> getAll(@PathVariable @NotNull final String token,
                              @DateTimeFormat(pattern = Constant.DATE_TIME_FORMAT)
                              @PathVariable @NotNull final Long dateTimeLastUpdate
    ) {
        final var device = deviceRepository.findByUuid(token);
        if (device.isPresent()) {
            if (device.get().getStatus() != Device.Status.ACTIVE) return ResponseEntityUtils.returnNonAuthoritativeInformation(List.of());
            if (device.get().getUser().getStatus() != User.Status.ACTIVE) return ResponseEntityUtils.returnNonAuthoritativeInformation(List.of());
            final var ret = repository.findByUserAndTimestampLastUpdateGreaterThan(device.get().getUser(), dateTimeLastUpdate);
            ret.forEach(it -> it.setServerId(it.getId()));
            return ResponseEntity.ok(ret);
        } else return ResponseEntityUtils.returnNoContent(List.of());
    }

    @PostMapping("/{token}")
    public ResponseEntity<Iterable<T>> insert(@PathVariable final String token,
                         @Valid @NotNull @RequestBody final List<T> list
    ) {
        return update(token, list);
    }

    @PutMapping("/{token}")
    public ResponseEntity<Iterable<T>> update(@PathVariable @NotNull final String token,
                         @Valid @NotNull @RequestBody final List<T> list
    ) {
        List<T> ret = new ArrayList<>();

        final var now = Instant.now(Clock.systemUTC()).getEpochSecond();

        final var device = deviceRepository.findByUuid(token);
        if (device.isPresent()) {
            if (device.get().getStatus() != Device.Status.ACTIVE) return ResponseEntityUtils.returnNonAuthoritativeInformation(List.of());
            if (device.get().getUser().getStatus() != User.Status.ACTIVE) return ResponseEntityUtils.returnNonAuthoritativeInformation(List.of());


            list.forEach(it -> {

                it.setUser(device.get().getUser());

                final var id = it.getId();
                it.setId(it.getServerId());

                it.setTimestampLastUpdate(now);

                try {

                    AtomicReference<T> original = new AtomicReference<>();
                    Optional.ofNullable(onSave).ifPresent(onSave -> original.set(onSave.perform(it)));

                    @SuppressWarnings("unchecked") final var base = (T) repository.save(it).clone();

                    base.setServerId(base.getId());
                    base.setId(id);

                    Optional.ofNullable(onReturn).ifPresent(onReturn -> onReturn.perform(original.get(), base));

                    ret.add(base);
                } catch (CloneNotSupportedException e) {
                    throw new RuntimeException(e);
                }

            });

            if (!ret.isEmpty()) {
                device.get().setTimestampLastUpdate(now);
                final var deviceChecked = deviceRepository.save(device.get());

                deviceChecked.setTimestampLastUpdate(now);
                userRepository.save(deviceChecked.getUser());
            }

            return ResponseEntity.ok(ret);
        } else return ResponseEntityUtils.returnNoContent(List.of());
    }


    @DeleteMapping("/{token}/{id}/{serverId}")
    public ResponseEntity<Iterable<T>> delete(@PathVariable @NotNull final String token,
                         @PathVariable @NotNull final Long id,
                         @PathVariable @NotNull final Long serverId
    ) {

        final var device = deviceRepository.findByUuid(token);
        if (device.isPresent()) {

            final var optional = repository.findByUserAndId(device.get().getUser(), serverId);
            if (optional.isPresent()) {

                T t = optional.get();
                t.setId(serverId);
                t.setServerId(id);

                final var date = Instant.now(Clock.systemUTC()).getEpochSecond();

                t.setDeleted(true);
                t.setTimestampLastUpdate(date);

                final var base = repository.save(t);

                device.get().setTimestampLastUpdate(date);
                final var deviceChecked = deviceRepository.save(device.get());

                deviceChecked.setTimestampLastUpdate(date);
                userRepository.save(deviceChecked.getUser());

                base.setServerId(serverId);
                base.setId(id);

                return ResponseEntity.ok(List.of(base));
            }
            return ResponseEntityUtils.returnNoContent(List.of());
        }
        return ResponseEntityUtils.returnNonAuthoritativeInformation(List.of());
    }

    @ExceptionHandler
    public void exceptionHandlerImpl(@NotNull final Exception e,
                                     @NotNull final HttpServletResponse response) throws Exception {
        exceptionHandler(e, response);
    }

    public static void exceptionHandler(@NotNull final Exception e,
                                        @NotNull final HttpServletResponse response) throws Exception {
        final var str = new StringBuilder("\n" + e);
        for (final var elm : e.getStackTrace()) {
            str.append("\n")
                    .append("\t")
                    .append(elm);
        }
        log.severe(str.toString());
        response.sendError(HttpStatus.BAD_REQUEST.value(), e.getLocalizedMessage());
    }

}
