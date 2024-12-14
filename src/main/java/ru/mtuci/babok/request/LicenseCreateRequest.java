package ru.mtuci.babok.request;

import lombok.Data;

@Data
public class LicenseCreateRequest
{
    private Long CreatorId;
    private Integer device_count;
    private String current_device;

}