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
import it.salsi.pocket.models.GroupField;
import it.salsi.pocket.repositories.DeviceRepository;
import it.salsi.pocket.repositories.GroupFieldRepository;
import it.salsi.pocket.repositories.GroupRepository;
import it.salsi.pocket.repositories.UserRepository;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Log
@Service
public final class GroupFieldController extends BaseController<GroupField, GroupFieldRepository> {

    public GroupFieldController(
            @NotNull final GroupFieldRepository repository,
            @NotNull final GroupRepository groupRepository,
            @NotNull final DeviceRepository deviceRepository,
            @NotNull final UserRepository userRepository
    ) {
        super(repository, deviceRepository, userRepository);

        setOnStore((@NotNull final var mapIdObjects, @NotNull final var groupField)  -> {

            final var tmp = groupField.getGroupId();
            groupField.setGroupId(groupField.getServerGroupId());
            groupField.setServerGroupId(tmp);

            groupRepository.findById(groupField.getGroupId()).ifPresent(group -> {
                groupField.setGroup(group);

                Optional.ofNullable(group.getGroupFields()).ifPresentOrElse(
                        groupFields -> groupFields.add(groupField),
                        () -> {
                            if (group.getGroupFields() != null) {
                                group.setGroupFields(new ArrayList<>());
                                group.getGroupFields().add(groupField);
                            }
                        }
                );
            });

            return groupField;
        });
    }
}
