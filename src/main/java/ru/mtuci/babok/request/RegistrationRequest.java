package ru.mtuci.babok.request;

import lombok.Data;

@Data
public class RegistrationRequest {
    private String login, email, password;
}
