package ru.mtuci.babok.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import ru.mtuci.babok.model.ApplicationRole;
import ru.mtuci.babok.model.ApplicationUser;
import ru.mtuci.babok.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl {
    private final UserRepository userRepository;

    public boolean saveUser(ApplicationUser user, String password)
    {
        Optional<ApplicationUser> userBD = userRepository.findByLogin(user.getLogin());

        if (userBD.isPresent()) return false;

        user.setRole(ApplicationRole.USER);
        user.setPassword(user.getPassword());
        user.setLogin(user.getLogin());

        userRepository.save(user);
        return true;
    }

}
