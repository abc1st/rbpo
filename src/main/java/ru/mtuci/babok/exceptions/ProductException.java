package ru.mtuci.babok.exceptions;

public abstract class ProductException extends RuntimeException {
    public ProductException(String msg) {
        super(msg);
    }
    public ProductException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
