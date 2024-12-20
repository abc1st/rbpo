package ru.mtuci.babok.repository;

import ru.mtuci.babok.model.LicenseType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LicenseTypeRepository extends JpaRepository<LicenseType, Long> {
}
