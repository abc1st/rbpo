package ru.mtuci.babok.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.babok.model.LicenseActivate;

import java.util.Optional;

public interface LicenseActivateRepository extends JpaRepository<LicenseActivate, Long> {
    Optional<LicenseActivate> findByCode(String code);
}
