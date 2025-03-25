package ru.mtuci.babok.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.babok.model.SignatureEntity;
import ru.mtuci.babok.model.SignatureStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SignatureRepository extends JpaRepository<SignatureEntity, UUID> {
    List<SignatureEntity> findByStatus(SignatureStatus status);
    List<SignatureEntity> findByUpdatedAtAfter(LocalDateTime since);
    List<SignatureEntity> findByIdIn(List<UUID> ids);
}