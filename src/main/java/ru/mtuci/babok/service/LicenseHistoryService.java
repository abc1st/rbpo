package ru.mtuci.babok.service;

import ru.mtuci.babok.model.ApplicationUser;
import ru.mtuci.babok.model.License;
import ru.mtuci.babok.model.LicenseHistory;
import ru.mtuci.babok.request.DataLicenseHistoryRequest;

import java.util.List;
import java.util.Optional;

public interface LicenseHistoryService {
    boolean recordLicenseChange(
            License license, ApplicationUser owner,
            String status, String description);
    Optional<LicenseHistory> findById(Long id);

    // save
    LicenseHistory save(DataLicenseHistoryRequest request);

    // read
    List<LicenseHistory> getAll();

    // update
    LicenseHistory update(DataLicenseHistoryRequest request);

    // delete
    void delete(Long id);
}
