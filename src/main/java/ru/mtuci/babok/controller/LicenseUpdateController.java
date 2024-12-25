package ru.mtuci.babok.controller;

import ru.mtuci.babok.configuration.JwtTokenProvider;
import ru.mtuci.babok.exceptions.categories.AuthenticationErrorException;
import ru.mtuci.babok.exceptions.categories.UserNotFoundException;
import ru.mtuci.babok.model.ApplicationUser;
import ru.mtuci.babok.model.Ticket;
import ru.mtuci.babok.request.LicenseUpdateRequest;
import ru.mtuci.babok.service.impl.AuthenticationServiceImpl;
import ru.mtuci.babok.service.impl.LicenseServiceImpl;
import ru.mtuci.babok.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/licenseUpdate")
@RequiredArgsConstructor
public class LicenseUpdateController {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserServiceImpl userService;
    private final AuthenticationServiceImpl authenticationService;
    private final LicenseServiceImpl licenseService;

    @PostMapping
    public ResponseEntity<?> licenseUpdate(@RequestHeader("Authorization") String auth, @RequestBody LicenseUpdateRequest licenseUpdateRequest) {
        try {
            // Получить аутентифицированного пользователя
            String login = jwtTokenProvider.getUsername(auth.split(" ")[1]);
            ApplicationUser user = userService.getUserByLogin(login).orElseThrow(
                    () -> new UserNotFoundException("Пользователь не найден")
            );

            // Аунтентификация пользователя
            if (!authenticationService.authenticate(user, licenseUpdateRequest.getPassword()))
                throw new AuthenticationErrorException("Аутентификация не удалась");

            String durationAdd = licenseUpdateRequest.getDurationAdd();
            // запрос на продление
            List<Ticket> tickets = licenseService.licenseRenewal(licenseUpdateRequest.getCodeActivation(), user, durationAdd);

            return ResponseEntity.ok(tickets);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(String.format("Ошибка(%s)", e.getMessage()));
        }
    }
}