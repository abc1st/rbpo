package ru.mtuci.babok.service;

import ru.mtuci.babok.model.LicenseActivate;
import ru.mtuci.babok.request.LicenseRequest;

import java.util.List;

public interface LicenseService {
    LicenseActivate createLicense(
        Long CreatorId, int device_count
        );

    LicenseActivate save(LicenseRequest request);

    List<LicenseActivate> getAll();

    void delete(Long id);
}
