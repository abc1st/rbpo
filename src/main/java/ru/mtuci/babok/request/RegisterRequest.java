package ru.mtuci.babok.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String login, password;
}
