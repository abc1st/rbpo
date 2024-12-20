package ru.mtuci.babok.exceptions.categories;

import ru.mtuci.babok.exceptions.ProductException;

public class ProductNotFoundException extends ProductException {
    public ProductNotFoundException(String msg) {
        super(msg);
    }
    public ProductNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
