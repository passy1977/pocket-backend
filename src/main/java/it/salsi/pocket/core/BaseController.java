package it.salsi.pocket.core;

import it.salsi.pocket.Constant;
import it.salsi.pocket.ResponseEntityUtils;
import it.salsi.pocket.models.Device;
import it.salsi.pocket.models.User;
import it.salsi.pocket.repositories.DeviceRepository;
import it.salsi.pocket.repositories.UserRepository;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Log
public class BaseController <T extends BaseModel, Y extends BaseRepository<T>> {

    @NotNull
    final private Y repository;

    @NotNull
    final private UserRepository userRepository;

    @NotNull
    final private DeviceRepository deviceRepository;

    public BaseController(@Autowired @NotNull final Y repository,
                    @Autowired @NotNull final DeviceRepository deviceRepository,
                    @Autowired @NotNull final UserRepository userRepository
    ) {
        this.repository = repository;
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
    }


    public Iterable<T> getAll(@NotNull final String token,
                              @NotNull final Long timestampLastUpdate
    ) {
        final var device = deviceRepository.findByUuid(token);
        if (device.isPresent()) {
            if (device.get().getStatus() != Device.Status.ACTIVE) return List.of();
            if (device.get().getUser().getStatus() != User.Status.ACTIVE) return List.of();
            final var ret = repository.findByUserAndTimestampLastUpdateGreaterThan(device.get().getUser(), timestampLastUpdate);
            ret.forEach(it -> it.setServerId(it.getId()));
            return ret;
        } else return List.of();
    }


}
