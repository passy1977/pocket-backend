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
import it.salsi.pocket.models.Group;
import it.salsi.pocket.repositories.DeviceRepository;
import it.salsi.pocket.repositories.GroupRepository;
import it.salsi.pocket.repositories.UserRepository;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Log
@Service
public final class GroupController extends BaseController<Group, GroupRepository> {

    public GroupController(
            @Autowired @NotNull final GroupRepository repository,
            @Autowired @NotNull final DeviceRepository deviceRepository,
            @Autowired @NotNull final UserRepository userRepository
    ) {
        super(repository, deviceRepository, userRepository);

        setOnStore( (@NotNull final var mapIdObjects, @NotNull final var group) -> {

            if(group.getServerGroupId() == 0 && mapIdObjects.containsKey(group.getGroupId())) {
                group.setServerGroupId(mapIdObjects.get(group.getGroupId()));
            }

            final var tmp = group.getGroupId();
            group.setGroupId(group.getServerGroupId());
            group.setServerGroupId(tmp);

            repository.findById(group.getGroupId()).ifPresent(parent -> {
                group.setGroup(parent);

                Optional.ofNullable(parent.getGroups()).ifPresentOrElse(
                        groups -> groups.add(group),
                        () -> {
                            if (group.getGroups() != null) {
                                group.setGroups(new ArrayList<>());
                                group.getGroups().add(group);
                            }
                        }
                );
            });

            return group;
        });

    }


}