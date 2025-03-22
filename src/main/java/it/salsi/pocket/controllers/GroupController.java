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
import it.salsi.pocket.models.Device;
import it.salsi.pocket.models.Group;
import it.salsi.pocket.models.User;
import it.salsi.pocket.repositories.DeviceRepository;
import it.salsi.pocket.repositories.GroupRepository;
import it.salsi.pocket.repositories.UserRepository;
import it.salsi.pocket.security.EncoderHelper;
import it.salsi.pocket.security.RSAHelper;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Log
@Service
public final class GroupController extends BaseController<Group, GroupRepository> {

    @NotNull
    private final Map<Long, Long> mapId = new HashMap<>();

    private @NotNull final GroupRepository repository;
    private @NotNull final DeviceRepository deviceRepository;

    public GroupController(
            @Autowired @NotNull final GroupRepository repository,
            @Autowired @NotNull final DeviceRepository deviceRepository,
            @Autowired @NotNull final UserRepository userRepository
    ) {
        super(repository, deviceRepository, userRepository);
        this.repository = repository;
        this.deviceRepository = deviceRepository;

        setOnStore( (@NotNull final var group) -> {

            if(group.getServerGroupId() == 0 && mapId.containsKey(group.getGroupId())) {
                group.setServerGroupId(mapId.get(group.getGroupId()));
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

    public void add(final @NotNull Group group) {
        add(group.getId(), group.getServerId());
    }

    @Override
    public void changePasswd(@NotNull final User user, @NotNull final Crypto aes) throws CommonsException {
        for(var it : repository.findByUser(user)) {
            it.setTitle(aes.encryptToString(it.getTitle()));
            it.setNote(aes.encryptToString(it.getNote()));
            it.setIcon(aes.encryptToString(it.getIcon()));
        }
    }
}