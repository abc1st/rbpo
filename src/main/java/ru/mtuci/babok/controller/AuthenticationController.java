package ru.mtuci.babok.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mtuci.babok.model.ApplicationUser;
import ru.mtuci.babok.model.AuthenticationResponse;
import ru.mtuci.babok.model.UserSession;
import ru.mtuci.babok.repository.UserRepository;
import ru.mtuci.babok.request.AuthenticationRequest;
import ru.mtuci.babok.request.RefreshRequest;
import ru.mtuci.babok.service.impl.TokenServiceImpl;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationManager authenticationManager;
    private final TokenServiceImpl tokenService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword()));
            ApplicationUser user = userRepository.findByLogin(request.getLogin())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            UserSession session = tokenService.issueTokenPair(user, request.getDeviceId());
            return ResponseEntity.ok(new AuthenticationResponse(session.getAccessToken(), session.getRefreshToken(), user.getLogin()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Неверный логин или пароль");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request) {
        Optional<UserSession> newSessionOpt = tokenService.refreshTokenPair(request.getRefreshToken(), request.getDeviceId());
        if (newSessionOpt.isPresent()) {
            UserSession newSession = newSessionOpt.get();
            return ResponseEntity.ok(new AuthenticationResponse(newSession.getAccessToken(), newSession.getRefreshToken(), newSession.getEmail()));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Недействительный refresh токен или deviceId");
        }
    }
}