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
import it.salsi.pocket.models.User;
import it.salsi.pocket.repositories.UserRepository;
import it.salsi.pocket.security.PasswordEncoder;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Log
@Service
public final class UserManagerImpl implements UserManager {

    @NotNull
    private final UserRepository userRepository;

    @NotNull
    private final PasswordEncoder passwordEncoder;

    @Value("${server.auth.user}")
    @Nullable
    private String user;

    @Value("${server.auth.passwd}")
    @Nullable
    private String passwd;

    public UserManagerImpl(@Autowired @NotNull final UserRepository userRepository,
                           @Autowired @NotNull final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public void checkAll() {
        log.info("Start user");

        if (user != null && passwd != null) {
            userRepository.findByEmail(user).ifPresentOrElse(user -> {
                        if (!user.getPasswd().equals(passwordEncoder.encode(passwd))) {
                            userRepository.delete(user);
                            userRepository.save(new User("ADMIN", this.user, passwordEncoder.encode(passwd)));
                        }
                    },
                    () -> userRepository.save(new User("ADMIN", user, passwordEncoder.encode(passwd)))
            );
        }

        log.info("End user");
    }
}
