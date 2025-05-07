package ru.mtuci.babok.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.babok.model.SignatureAudit;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SignatureAuditRepository extends JpaRepository<SignatureAudit, Long> {
    List<SignatureAudit> findBySignatureId(UUID signatureId);
    List<SignatureAudit> findByChangeType(String changeType);
    List<SignatureAudit> findByChangedBy(String changedBy);
    List<SignatureAudit> findByChangedAtBetween(LocalDateTime start, LocalDateTime end);
}