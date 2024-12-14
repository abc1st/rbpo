package ru.mtuci.babok.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LicenseRequest {
    private Long id;
    private int device_count;
}
