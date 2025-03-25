package ru.mtuci.babok.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.babok.model.SignatureAudit;

public interface SignatureAuditRepository extends JpaRepository<SignatureAudit, Long> {
}