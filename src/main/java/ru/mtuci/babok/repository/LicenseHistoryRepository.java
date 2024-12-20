package ru.mtuci.babok.repository;

import ru.mtuci.babok.model.LicenseHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LicenseHistoryRepository extends JpaRepository<LicenseHistory, Long> {
}
