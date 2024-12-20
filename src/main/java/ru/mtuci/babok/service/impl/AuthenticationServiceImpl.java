package ru.mtuci.babok.service.impl;

import ru.mtuci.babok.model.ApplicationUser;
import ru.mtuci.babok.service.AutheneticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AutheneticationService {
    private final AuthenticationManager authenticationManager;

    @Override
    public boolean authenticate(ApplicationUser user, String password) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getLogin(), password)
        ).isAuthenticated();
    }
}
