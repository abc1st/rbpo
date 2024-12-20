package ru.mtuci.babok.exceptions.categories.License;

import ru.mtuci.babok.exceptions.LicenseException;

public class LicenseErrorActivationException extends LicenseException {
    public LicenseErrorActivationException(String msg) {
        super(msg);
    }
    public LicenseErrorActivationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
