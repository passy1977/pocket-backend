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


package it.salsi.pocket.controllers;


import it.salsi.commons.CommonsException;
import it.salsi.commons.utils.Crypto;
import it.salsi.pocket.core.BaseController;
import it.salsi.pocket.models.Field;
import it.salsi.pocket.models.User;
import it.salsi.pocket.repositories.*;
import lombok.Setter;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

@Setter
@Log
@Service
public final class FieldController extends BaseController<Field, FieldRepository> {

    @Nullable
    private Map<Long, Long> groupMapId = null;

    @Nullable
    private Map<Long, Long> groupFieldMapId = null;

    private @NotNull final FieldRepository repository;
    private @NotNull final DeviceRepository deviceRepository;

    public FieldController(
            @Autowired @NotNull final FieldRepository repository,
            @Autowired @NotNull final GroupRepository groupRepository,
            @Autowired @NotNull final GroupFieldRepository groupFieldRepository,
            @Autowired @NotNull final DeviceRepository deviceRepository,
            @Autowired @NotNull final UserRepository userRepository
    ) {
        super(repository, deviceRepository, userRepository);
        this.repository = repository;
        this.deviceRepository = deviceRepository;

        setOnStore((@NotNull final var field) -> {

            if(groupMapId != null && field.getServerGroupId() == 0 && groupMapId.containsKey(field.getGroupId())) {
                field.setServerGroupId(groupMapId.get(field.getGroupId()));
            }

            var tmp = field.getGroupId();
            field.setGroupId(field.getServerGroupId());
            field.setServerGroupId(tmp);

            groupRepository.findById(field.getGroupId()).ifPresent(group -> {
                field.setGroup(group);

                Optional.ofNullable(group.getFields()).ifPresentOrElse(
                        fields -> fields.add(field),
                        () -> {
                            if (group.getFields() != null) {
                                group.setFields(new ArrayList<>());
                                group.getFields().add(field);
                            }
                        }
                );
            });

            if(groupFieldMapId != null && field.getServerGroupFieldId() == 0 && groupFieldMapId.containsKey(field.getGroupFieldId())) {
                field.setServerGroupFieldId(groupFieldMapId.get(field.getGroupFieldId()));
            }

            tmp = field.getGroupFieldId();
            field.setGroupFieldId(field.getServerGroupFieldId());
            field.setServerGroupFieldId(tmp);

            return field;
        });
    }

    @Override
    public void changePasswd(@NotNull final User user, @NotNull final Crypto aesOld, @NotNull final Crypto aesNew) throws CommonsException {
        for(var it : repository.findByUser(user)) {
            it.setTitle(aesNew.encryptToString(aesOld.decryptToString(it.getTitle())));
            it.setValue(aesNew.encryptToString(aesOld.decryptToString(it.getValue())));
        }
    }
}
