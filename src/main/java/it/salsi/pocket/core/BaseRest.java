/*
The MIT License (MIT)

Original Work Copyright (c) 2018-2025 Antonio Salsi

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

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

        final var device = deviceRepository.findByToken(token);
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
                              @PathVariable @NotNull final Date dateTimeLastUpdate
    ) {
        final var device = deviceRepository.findByToken(token);
        if (device.isPresent()) {
            if (device.get().getStatus() != Device.Status.ACTIVE) return ResponseEntityUtils.returnNonAuthoritativeInformation(List.of());
            if (device.get().getUser().getStatus() != User.Status.ACTIVE) return ResponseEntityUtils.returnNonAuthoritativeInformation(List.of());
            final var ret = repository.findByUserAndDateTimeLastUpdateGreaterThan(device.get().getUser(), dateTimeLastUpdate);
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

        final var device = deviceRepository.findByToken(token);
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

                deviceChecked.getUser().setTimestampLastUpdate(now);
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

        final var device = deviceRepository.findByToken(token);
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

                deviceChecked.getUser().setTimestampLastUpdate(date);
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
