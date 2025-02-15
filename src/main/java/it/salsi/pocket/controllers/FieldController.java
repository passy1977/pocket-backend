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


import it.salsi.pocket.core.BaseController;
import it.salsi.pocket.models.Field;
import it.salsi.pocket.repositories.DeviceRepository;
import it.salsi.pocket.repositories.FieldRepository;
import it.salsi.pocket.repositories.GroupRepository;
import it.salsi.pocket.repositories.UserRepository;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

@Log
@Service
public final class FieldController extends BaseController<Field, FieldRepository> {

    public FieldController(
            @Autowired @NotNull final FieldRepository repository,
            @Autowired @NotNull final GroupRepository groupRepository,
            @Autowired @NotNull final DeviceRepository deviceRepository,
            @Autowired @NotNull final UserRepository userRepository
    ) {
        super(repository, deviceRepository, userRepository);

        setOnStore((@NotNull final var mapIdObjects, @NotNull final var field) -> {

            var tmp = field.getGroupId();
            field.setGroupId(field.getServerGroupId());
            field.setServerGroupId(tmp);

            tmp = field.getGroupFieldId();
            field.setGroupFieldId(field.getServerGroupFieldId());
            field.setServerGroupFieldId(tmp);

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

            return field;
        });

    }
}
