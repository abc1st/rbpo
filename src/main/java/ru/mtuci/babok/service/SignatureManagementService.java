package ru.mtuci.babok.service;

import ru.mtuci.babok.model.SignatureAudit;
import ru.mtuci.babok.model.SignatureEntity;
import ru.mtuci.babok.model.SignatureStatus;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SignatureManagementService {

    SignatureEntity createSignature(SignatureEntity entity, String changedBy) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException;
    SignatureEntity updateSignature(UUID id, SignatureEntity updatedEntity, String changedBy) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException;
    void deleteSignature(UUID id, String changedBy);
    List<SignatureEntity> getAllActualSignatures();
    List<SignatureEntity> getSignaturesUpdatedAfter(LocalDateTime since);
    List<SignatureEntity> getSignaturesByIds(List<UUID> ids);
    List<SignatureEntity> getSignaturesByStatus(SignatureStatus status);
    List<SignatureAudit> getAllAuditRecords();
    List<SignatureAudit> getAuditRecordsBySignatureId(UUID signatureId);
    List<SignatureAudit> getAuditRecordsByChangeType(String changeType);
    List<SignatureAudit> getAuditRecordsByChangedBy(String changedBy);
    List<SignatureAudit> getAuditRecordsByDateRange(LocalDateTime start, LocalDateTime end);
    byte[] serializeSignatureFields(SignatureEntity signature);
    byte[] generateManifestSignature(int count, byte[] massiveSignature) throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeyException;
}
