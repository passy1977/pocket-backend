/*
The MIT License (MIT)

Original Work Copyright (c) 2018-2025 Antonio Salsi

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package it.salsi.pocket.services;

import it.salsi.commons.CommonsException;
import it.salsi.pocket.Constant;
import it.salsi.pocket.models.Property;
import it.salsi.pocket.models.User;
import it.salsi.pocket.repositories.PropertyRepository;
import it.salsi.pocket.repositories.UserRepository;
import it.salsi.pocket.security.PasswordEncoder;
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

    @NotNull
    private final PasswordEncoder passwordEncoder;

    public PropertiesManagerImpl(@Autowired @NotNull final PropertyRepository propertyRepository,
                                 @Autowired @NotNull final UserRepository userRepository,
                                 @Autowired @NotNull final PasswordEncoder passwordEncoder) {
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void checkAll() throws CommonsException {
        if(authUser == null || authPasswd == null) {
            throw new CommonsException("authUser or authPasswd not set");
        }

        log.info("Start checks");


        AtomicReference<User> adminUser = new AtomicReference<>(new User());
        userRepository.findByEmail(authUser).ifPresentOrElse(
                adminUser::set,
                () -> adminUser.set(userRepository.save(new User(authUser, authUser, passwordEncoder.encode(authPasswd))))
        );


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
