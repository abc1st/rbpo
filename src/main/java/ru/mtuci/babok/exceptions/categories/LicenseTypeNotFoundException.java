package ru.mtuci.babok.exceptions.categories;

import ru.mtuci.babok.exceptions.LicenseTypeException;

public class LicenseTypeNotFoundException extends LicenseTypeException {
    public LicenseTypeNotFoundException(String msg) {
        super(msg);
    }
    public LicenseTypeNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
