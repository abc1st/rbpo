package ru.mtuci.babok.exceptions.categories;

import ru.mtuci.babok.exceptions.LicenseHistoryException;

public class LicenseHistoryNotFoundException extends LicenseHistoryException {
    public LicenseHistoryNotFoundException(String msg) {
        super(msg);
    }
    public LicenseHistoryNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
