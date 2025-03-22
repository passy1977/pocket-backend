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

package it.salsi.pocket.services;

import it.salsi.commons.CommonsException;
import it.salsi.pocket.Constant;
import it.salsi.pocket.models.Property;
import it.salsi.pocket.models.User;
import it.salsi.pocket.repositories.PropertyRepository;
import it.salsi.pocket.repositories.UserRepository;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Log
@Service
public final class PropertiesManagerImpl implements PropertiesManager {

    @Value("${server.auth.user}")
    @Nullable
    private String authUser;

    @Value("${server.auth.passwd}")
    @Nullable
    private String authPasswd;

    @NotNull
    private final PropertyRepository propertyRepository;

    @NotNull
    private final UserRepository userRepository;

    public PropertiesManagerImpl(@Autowired @NotNull final PropertyRepository propertyRepository,
                                 @Autowired @NotNull final UserRepository userRepository) {
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void checkAll() throws CommonsException {
        if(authUser == null || authPasswd == null) {
            throw new CommonsException("authUser or authPasswd not set");
        }
        if(authPasswd.length() != 32) {
            throw new CommonsException("authPasswd must be 32 byte");
        }

        log.info("Start checks");


        AtomicReference<User> adminUser = new AtomicReference<>(new User());
        userRepository.findByEmail(authUser).ifPresent(adminUser::set);


        for (final var constant : Constant.values()) {
            final var metaProperty = constant.getMetaProperty();

            if (metaProperty.mandatory()) {
                final var opt = propertyRepository.getByUserIdAndKey(adminUser.get().getId(), constant);
                if (opt.isEmpty()) {
                    var property = new Property();
                    property.setKey(metaProperty.constant());

                    Optional.ofNullable(
                            metaProperty.defaultValue()
                    ).ifPresentOrElse(
                            property::setValue,
                            () -> property.setValue("")
                    );

                    property.setUser(adminUser.get());
                    property.setType(metaProperty.type());
                    propertyRepository.save(property);
                    log.info("add property: " + property);
                }
            }
        }

        log.info("End checks");
    }

}
