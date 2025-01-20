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
