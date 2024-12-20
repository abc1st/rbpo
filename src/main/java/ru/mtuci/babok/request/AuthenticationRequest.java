package ru.mtuci.babok.request;

import lombok.Data;

@Data
public class AuthenticationRequest {
    private String login, password;
}
