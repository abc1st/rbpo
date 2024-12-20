package ru.mtuci.babok.request;

import lombok.Data;

@Data
public class LicenseUpdateRequest {
    private String password, codeActivation;
}
