package ru.mtuci.babok.request;

import lombok.Data;

@Data
public class DeviceRequest {
    private String activationCode, name, macAddress;
}
