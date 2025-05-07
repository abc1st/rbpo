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
import java.util.Arrays;
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

        boolean isChanged = false;
        StringBuilder changedFields = new StringBuilder();

        if (updatedEntity.getThreatName() != null && !updatedEntity.getThreatName().equals(existing.getThreatName())) {
            existing.setThreatName(updatedEntity.getThreatName());
            addChangedField(changedFields, "threat_name");
            isChanged = true;
        }

        if (updatedEntity.getOffsetStart() != null && !updatedEntity.getOffsetStart().equals(existing.getOffsetStart())) {
            existing.setOffsetStart(updatedEntity.getOffsetStart());
            addChangedField(changedFields, "offset_start");
            isChanged = true;
        }

        if (updatedEntity.getOffsetEnd() != null && !updatedEntity.getOffsetEnd().equals(existing.getOffsetEnd())) {
            existing.setOffsetEnd(updatedEntity.getOffsetEnd());
            addChangedField(changedFields, "offset_end");
            isChanged = true;
        }


        if (updatedEntity.getFileType() != null && !updatedEntity.getFileType().equals(existing.getFileType())) {
            existing.setFileType(updatedEntity.getFileType());
            addChangedField(changedFields, "file_type");
            isChanged = true;
        }

        if (updatedEntity.getFirstBytes() != null && !Arrays.equals(updatedEntity.getFirstBytes(), existing.getFirstBytes())) {
            existing.setFirstBytes(updatedEntity.getFirstBytes());
            addChangedField(changedFields, "first_bytes");
            isChanged = true;
        }

        if (updatedEntity.getRemainderHash() != null && !updatedEntity.equals(existing.getRemainderHash())) {
            existing.setRemainderHash(updatedEntity.getRemainderHash());
            addChangedField(changedFields, "remainder_hash");
            isChanged = true;
        }

        if (updatedEntity.getRemainderLength() != null && !updatedEntity.getRemainderLength().equals(existing.getRemainderLength())) {
            existing.setRemainderLength(updatedEntity.getRemainderLength());
            addChangedField(changedFields, "remainder_length");
            isChanged = true;
        }

        if (isChanged){
            existing.setUpdatedAt(LocalDateTime.now());

            byte[] dataToSign = signatureService.getDataToSign(existing);
            byte [] signature = signatureService.sign(dataToSign);
            existing.setDigitalSignature(signature);

            SignatureEntity updated = signatureRepository.save(existing);
            saveAudit(updated, "UPDATED", changedBy, changedFields.toString());
            return updated;
        }
        else {
           return existing;
        }

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

    private void addChangedField(StringBuilder changedFields, String fieldName) {
        if (changedFields.length() > 0) {
            changedFields.append(", ");
        }
        changedFields.append(fieldName);
    }
}