package ru.mtuci.babok.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mtuci.babok.model.*;
import ru.mtuci.babok.repository.SignatureAuditRepository;
import ru.mtuci.babok.repository.SignatureHistoryRepository;
import ru.mtuci.babok.repository.SignatureRepository;
import ru.mtuci.babok.service.impl.SignatureServiceImpl;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SignatureManagementServiceImpl {
    private final SignatureRepository signatureRepository;
    private final SignatureHistoryRepository signatureHistoryRepository;
    private final SignatureAuditRepository signatureAuditRepository;
    private final SignatureServiceImpl signatureService;

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public SignatureEntity createSignature(SignatureEntity entity, String changedBy) throws Exception {
        SignatureEntity newEntity = new SignatureEntity();
        newEntity.setThreatName(entity.getThreatName());
        newEntity.setFirstBytes(entity.getFirstBytes());
        newEntity.setRemainderHash(entity.getRemainderHash());
        newEntity.setRemainderLength(entity.getRemainderLength());
        newEntity.setFileType(entity.getFileType());
        newEntity.setOffsetStart(entity.getOffsetStart());
        newEntity.setOffsetEnd(entity.getOffsetEnd());
        newEntity.setUpdatedAt(LocalDateTime.now());
        newEntity.setStatus(SignatureStatus.ACTUAL);

        SignatureEntity savedEntity = signatureRepository.save(newEntity);

        byte[] dataToSign = signatureService.getDataToSign(savedEntity);
        byte[] signature = signatureService.sign(dataToSign);
        savedEntity.setDigitalSignature(signature);

        savedEntity = signatureRepository.save(savedEntity);
        saveAudit(savedEntity, "CREATED", changedBy, "");
        return savedEntity;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public SignatureEntity updateSignature(UUID id, SignatureEntity updatedEntity, String changedBy) throws Exception {
        SignatureEntity existing = signatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Сигнатура не найдена"));

        saveHistory(existing);

        existing.setThreatName(updatedEntity.getThreatName());
        existing.setFirstBytes(updatedEntity.getFirstBytes());
        existing.setRemainderHash(updatedEntity.getRemainderHash());
        existing.setRemainderLength(updatedEntity.getRemainderLength());
        existing.setFileType(updatedEntity.getFileType());
        existing.setOffsetStart(updatedEntity.getOffsetStart());
        existing.setOffsetEnd(updatedEntity.getOffsetEnd());
        existing.setUpdatedAt(LocalDateTime.now());

        byte[] dataToSign = signatureService.getDataToSign(existing);
        byte[] signature = signatureService.sign(dataToSign);
        existing.setDigitalSignature(signature);

        SignatureEntity updated = signatureRepository.save(existing);
        saveAudit(updated, "UPDATED", changedBy, "threat_name, first_bytes");
        return updated;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteSignature(UUID id, String changedBy) {
        SignatureEntity entity = signatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Сигнатура не найдена"));

        saveHistory(entity);

        entity.setStatus(SignatureStatus.DELETED);
        entity.setUpdatedAt(LocalDateTime.now());
        signatureRepository.save(entity);
        saveAudit(entity, "DELETED", changedBy, "");
    }

    @Transactional(readOnly = true)
    public List<SignatureEntity> getAllActualSignatures() {
        return signatureRepository.findByStatus(SignatureStatus.ACTUAL);
    }

    @Transactional(readOnly = true)
    public List<SignatureEntity> getSignaturesUpdatedAfter(LocalDateTime since) {
        return signatureRepository.findByUpdatedAtAfter(since);
    }

    @Transactional(readOnly = true)
    public List<SignatureEntity> getSignaturesByIds(List<UUID> ids) {
        return signatureRepository.findByIdIn(ids);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<SignatureEntity> getSignaturesByStatus(SignatureStatus status) {
        return signatureRepository.findByStatus(status);
    }

    private void saveHistory(SignatureEntity entity) {
        SignatureHistory history = new SignatureHistory();
        history.setSignatureId(entity.getId());
        history.setVersionCreatedAt(LocalDateTime.now());
        history.setThreatName(entity.getThreatName());
        history.setFirstBytes(entity.getFirstBytes());
        history.setRemainderHash(entity.getRemainderHash());
        history.setRemainderLength(entity.getRemainderLength());
        history.setFileType(entity.getFileType());
        history.setOffsetStart(entity.getOffsetStart());
        history.setOffsetEnd(entity.getOffsetEnd());
        history.setDigitalSignature(entity.getDigitalSignature());
        history.setUpdatedAt(entity.getUpdatedAt());
        history.setStatus(entity.getStatus());
        signatureHistoryRepository.save(history);
    }

    private void saveAudit(SignatureEntity entity, String changeType, String changedBy, String fieldsChanged) {
        SignatureAudit audit = new SignatureAudit();
        audit.setSignatureId(entity.getId());
        audit.setChangedBy(changedBy);
        audit.setChangeType(changeType);
        audit.setChangedAt(LocalDateTime.now());
        audit.setFieldsChanged(fieldsChanged);
        signatureAuditRepository.save(audit);
    }
}