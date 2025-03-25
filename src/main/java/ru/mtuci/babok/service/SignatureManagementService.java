package ru.mtuci.babok.service;

import ru.mtuci.babok.model.SignatureEntity;
import ru.mtuci.babok.model.SignatureStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SignatureManagementService {

    SignatureEntity createSignature(SignatureEntity entity, String changedBy);
    SignatureEntity updateSignature(UUID id, SignatureEntity updatedEntity, String changedBy);
    void deleteSignature(UUID id, String changedBy);
    List<SignatureEntity> getAllActualSignatures();
    List<SignatureEntity> getSignaturesUpdatedAfter(LocalDateTime since);
    List<SignatureEntity> getSignaturesByIds(List<UUID> ids);
    List<SignatureEntity> getSignaturesByStatus(SignatureStatus status);
}
