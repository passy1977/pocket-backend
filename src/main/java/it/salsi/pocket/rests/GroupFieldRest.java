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


package it.salsi.pocket.rests;

import it.salsi.pocket.core.BaseRest;
import it.salsi.pocket.models.GroupField;
import it.salsi.pocket.repositories.DeviceRepository;
import it.salsi.pocket.repositories.GroupFieldRepository;
import it.salsi.pocket.repositories.GroupRepository;
import it.salsi.pocket.repositories.UserRepository;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Deprecated
@Log
@RestController
@RequestMapping("${server.api-version}/groupField/")
public final class GroupFieldRest extends BaseRest<GroupField, GroupFieldRepository> {

    public GroupFieldRest(@Autowired @NotNull final GroupFieldRepository repository,
                          @Autowired @NotNull final GroupRepository groupRepository,
                          @Autowired @NotNull final DeviceRepository deviceRepository,
                          @Autowired @NotNull final UserRepository userRepository) {
        super(repository, deviceRepository, userRepository);

        setOnSave(groupField -> {
            if (groupField.getGroup() == null) {
                return groupField;
            }

            GroupField original;
            try {
                original = (GroupField) groupField.clone();
            } catch (CloneNotSupportedException e) {
                log.severe(e.getMessage());
                return groupField;
            }

            groupRepository.findById(groupField.getGroup().getServerId()).ifPresent(groupField::setGroup);

            return original;
        });

        setOnReturn((original, ret) -> ret.setGroup(original.getGroup()));
    }

}
