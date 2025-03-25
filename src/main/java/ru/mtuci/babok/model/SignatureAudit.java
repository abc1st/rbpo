package ru.mtuci.babok.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "signature_audit")
@Data
public class SignatureAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auditId;

    @Column(name = "signature_id")
    private UUID signatureId;

    @Column(name = "changed_by")
    private String changedBy;

    @Column(name = "change_type")
    private String changeType; // CREATED, UPDATED, DELETED, CORRUPTED

    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    @Column(name = "fields_changed")
    private String fieldsChanged;
}