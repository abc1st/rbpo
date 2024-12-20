package ru.mtuci.babok.exceptions.categories;

import ru.mtuci.babok.exceptions.AuthenticationException;

public class AuthenticationErrorException extends AuthenticationException {
    public AuthenticationErrorException(String msg) {
        super(msg);
    }
    public AuthenticationErrorException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
