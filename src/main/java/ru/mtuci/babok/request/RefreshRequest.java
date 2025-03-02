package ru.mtuci.babok.request;

import lombok.Data;

@Data
public class RefreshRequest {
    private String refreshToken;
    private String deviceId;
}