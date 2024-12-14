package ru.mtuci.babok.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mtuci.babok.model.ApplicationUser;
import ru.mtuci.babok.request.RegisterRequest;
import ru.mtuci.babok.service.impl.RegistrationServiceImpl;

@RestController
@RequestMapping("/register")
@RequiredArgsConstructor

public class RegisterController {
    private final RegistrationServiceImpl registrationService;
    @PostMapping
    public ResponseEntity<?> registration(@RequestBody RegisterRequest registerRequest)
    {
        ApplicationUser applicationUser = new ApplicationUser();
        applicationUser.setLogin(registerRequest.getLogin());

        if(!registrationService.saveUser(applicationUser, registerRequest.getPassword()))
            return ResponseEntity.badRequest().body("Пользователь уже зарегистрирован");

        return ResponseEntity.ok("Успешная регистрация");
    }
}
