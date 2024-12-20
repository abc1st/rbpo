package ru.mtuci.babok.exceptions.categories;

import ru.mtuci.babok.exceptions.DeviceLicenseException;

public class DeviceLicenseNotFoundException extends DeviceLicenseException {
    public DeviceLicenseNotFoundException(String msg) { super(msg); }
    public DeviceLicenseNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
