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
        log.info("start user");

        if (user != null) {
            userRepository.findByEmail(user).ifPresentOrElse(user ->
                    {
                        assert passwd != null;
                        if (!user.getPasswd().equals(passwordEncoder.encode(passwd))) {
                            userRepository.delete(user);
                            try {
                                insertBaseAuthUser(this.user, passwd);
                            } catch (CommonsException e) {
                                log.severe(e.getMessage());
                            }
                        }
                    },
                    () -> {
                        try {
                            insertBaseAuthUser(user, passwd);
                        } catch (CommonsException e) {
                            log.info(e.getMessage());
                        }
                    }
            );
        }

        log.info("end user");
    }


    private void insertBaseAuthUser(@Nullable final String user, @Nullable final String passwd) throws CommonsException {
        if (passwd == null || user == null) {
            throw new CommonsException("baseAuth parameters not founds");
        }
//        final var baseAuth = new User(BASE_AUTH.name(), user, passwordEncoder.encode(passwd));
//        roleRepository.findByRole(BASE_AUTH).ifPresent(role -> {
//            if (baseAuth.getRoles() == null) {
//                baseAuth.setRoles(new HashSet<>());
//            }
//            baseAuth.getRoles().add(role);
//            if (role.getUsers() == null) {
//                role.setUsers(new ArrayList<>());
//            }
//        });
        //userRepository.save(baseAuth);
    }
}
