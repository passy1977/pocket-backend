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
import it.salsi.pocket.models.GroupField;
import it.salsi.pocket.models.User;
import it.salsi.pocket.repositories.DeviceRepository;
import it.salsi.pocket.repositories.GroupFieldRepository;
import it.salsi.pocket.repositories.GroupRepository;
import it.salsi.pocket.repositories.UserRepository;
import lombok.Setter;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Setter
@Log
@Service
public final class GroupFieldController extends BaseController<GroupField, GroupFieldRepository> {

    @NotNull
    private final Map<Long, Long> mapId = new HashMap<>();

    @Nullable
    private Map<Long, Long> groupMapId = null;

    private @NotNull final GroupFieldRepository repository;
    private @NotNull final DeviceRepository deviceRepository;

    public GroupFieldController(
            @NotNull final GroupFieldRepository repository,
            @NotNull final GroupRepository groupRepository,
            @NotNull final DeviceRepository deviceRepository,
            @NotNull final UserRepository userRepository
    ) {
        super(repository, deviceRepository, userRepository);
        this.repository = repository;
        this.deviceRepository = deviceRepository;

        setOnStore((@NotNull final var groupField)  -> {

            if(groupMapId != null &&  groupField.getServerGroupId() == 0 && groupMapId.containsKey(groupField.getGroupId())) {
                groupField.setServerGroupId(groupMapId.get(groupField.getGroupId()));
            }

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


    public void clean() {
        mapId.clear();
    }

    @NotNull
    public Map<Long, Long> getMapId() {
        return mapId;
    }

    public void add(long id, long serverId) {
        if(!mapId.containsKey(id)) {
            mapId.put(id, serverId);
        }
    }

    public void add(final @NotNull GroupField groupField) {
        add(groupField.getId(), groupField.getServerId());
    }

    @Override
    public void changePasswd(@NotNull final User user, @NotNull final Crypto aesOld, @NotNull final Crypto aesNew, long now) throws CommonsException {
        for(var it : repository.findByUser(user)) {
            it.setTitle(aesNew.encryptToString(aesOld.decryptToString(it.getTitle())));
            it.setTimestampLastUpdate(now);
        }
    }
}
