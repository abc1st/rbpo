package ru.mtuci.babok.controller;

import ru.mtuci.babok.configuration.JwtTokenProvider;
import ru.mtuci.babok.model.ApplicationUser;
import ru.mtuci.babok.model.AuthenticationResponse;
import ru.mtuci.babok.repository.UserRepository;
import ru.mtuci.babok.request.AuthenticationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/login")
@RequiredArgsConstructor
public class AuthenticationController {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest request) {
        try {
            String login = request.getLogin();

            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(login, request.getPassword())
            );

            ApplicationUser user = userRepository.findByLogin(login).orElseThrow(() -> new UsernameNotFoundException("user not found"));

            String token = jwtTokenProvider.createToken(login, user.getRole().getGrantedAuthorities());

            return ResponseEntity.ok(new AuthenticationResponse(token, login));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid login or password");
        }
    }
}
