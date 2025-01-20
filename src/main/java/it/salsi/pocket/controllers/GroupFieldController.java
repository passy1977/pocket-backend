package it.salsi.pocket.controllers;

import it.salsi.pocket.core.BaseController;
import it.salsi.pocket.core.BaseRest;
import it.salsi.pocket.models.GroupField;
import it.salsi.pocket.repositories.DeviceRepository;
import it.salsi.pocket.repositories.GroupFieldRepository;
import it.salsi.pocket.repositories.UserRepository;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log
@Service
public final class GroupFieldController extends BaseController<GroupField, GroupFieldRepository> {

    public GroupFieldController(
            @NotNull final GroupFieldRepository repository,
            @NotNull final DeviceRepository deviceRepository,
            @NotNull final UserRepository userRepository
    ) {
        super(repository, deviceRepository, userRepository);
    }
}
