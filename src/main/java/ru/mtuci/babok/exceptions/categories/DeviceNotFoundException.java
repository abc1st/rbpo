package ru.mtuci.babok.exceptions.categories;

import ru.mtuci.babok.exceptions.DeviceException;

public class DeviceNotFoundException extends DeviceException {
    public DeviceNotFoundException(String msg) { super(msg); }
    public DeviceNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
