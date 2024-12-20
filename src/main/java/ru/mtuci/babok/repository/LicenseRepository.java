package ru.mtuci.babok.repository;

import ru.mtuci.babok.model.License;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LicenseRepository extends JpaRepository<License, Long> {
    Optional<License> findByCode(String licenseCode);
    List<License> findAllByCode(String licenseCode);
}
