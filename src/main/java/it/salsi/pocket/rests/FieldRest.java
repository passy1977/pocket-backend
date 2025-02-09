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
import it.salsi.pocket.models.Field;
import it.salsi.pocket.repositories.DeviceRepository;
import it.salsi.pocket.repositories.FieldRepository;
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
@RequestMapping("${server.api-version}/field/")
public final class FieldRest extends BaseRest<Field, FieldRepository> {

    public FieldRest(@Autowired @NotNull final FieldRepository repository,
                     @Autowired @NotNull final GroupRepository groupRepository,
                     @Autowired @NotNull final DeviceRepository deviceRepository,
                     @Autowired @NotNull final UserRepository userRepository) {
        super(repository, deviceRepository, userRepository);

        setOnSave(field -> {
            if (field.getGroup() == null) {
                return field;
            }

            Field original;
            try {
                original = (Field) field.clone();
            } catch (CloneNotSupportedException e) {
                log.severe(e.getMessage());
                return field;
            }

            field.setGroupFieldId(original.getServerGroupFieldId());

            groupRepository.findById(field.getGroup().getServerId()).ifPresent(field::setGroup);

            return original;
        });

        setOnReturn((original, ret) -> {
            ret.setGroup(original.getGroup());
            ret.setGroupFieldId(original.getGroupFieldId());
        });
    }

}
