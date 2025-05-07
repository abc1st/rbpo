package ru.mtuci.babok.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mtuci.babok.model.SignatureAudit;
import ru.mtuci.babok.model.SignatureEntity;
import ru.mtuci.babok.model.SignatureStatus;
import ru.mtuci.babok.repository.SignatureAuditRepository;
import ru.mtuci.babok.repository.SignatureRepository;
import ru.mtuci.babok.service.SignatureVerificationService;
import ru.mtuci.babok.service.impl.SignatureServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SignatureVerificationServiceImpl implements SignatureVerificationService {
    private static final Logger logger = LoggerFactory.getLogger(SignatureVerificationServiceImpl.class);
    private final SignatureRepository signatureRepository;
    private final SignatureServiceImpl signatureService;
    private final SignatureAuditRepository signatureAuditRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void verifySignatures() {
        LocalDateTime lastCheck = LocalDateTime.now().minusDays(1);
        List<SignatureEntity> signatures = signatureRepository.findByUpdatedAtAfter(lastCheck);

        for (SignatureEntity signature : signatures) {
            try {
                byte[] data = signatureService.getDataToSign(signature);
                boolean isValid = signatureService.verify(data, signature.getDigitalSignature());
                if (!isValid) {
                    signature.setStatus(SignatureStatus.CORRUPTED);
                    signatureRepository.save(signature);
                    logger.error("ЭЦП сигнатуры {} повреждена", signature.getId());
                    saveAudit(signature, "CORRUPTED", "system", "ЭЦП не прошла проверку");
                }
            } catch (Exception e) {
                logger.error("Ошибка проверки ЭЦП для сигнатуры {}: {}", signature.getId(), e.getMessage());
            }
        }
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