package ru.mtuci.babok.repository;

import ru.mtuci.babok.model.Device;
import ru.mtuci.babok.model.DeviceLicense;
import ru.mtuci.babok.model.License;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceLicenseRepository extends JpaRepository<DeviceLicense, Long> {
    Optional<DeviceLicense> findByDeviceAndLicense(Device device, License license);
}
