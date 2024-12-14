package ru.mtuci.babok.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LicenseCreateRequest
{
    private Long CreatorId;
    private Integer device_count;
    private String current_device;
    private LocalDateTime lifeTime;
}