package ru.mtuci.babok.request;

import lombok.Data;

@Data
public class DeviceInfoRequest {
    private String name, macAddress;
}
