package ru.mtuci.babok.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "signatures")
@Data
public class SignatureEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "threat_name")
    private String threatName;

    @Column(name = "first_bytes", columnDefinition = "BYTEA")
    private byte[] firstBytes;

    @Column(name = "remainder_hash")
    private String remainderHash;

    @Column(name = "remainder_length")
    private int remainderLength;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "offset_start")
    private int offsetStart;

    @Column(name = "offset_end")
    private int offsetEnd;

    @Column(name = "digital_signature", columnDefinition = "BYTEA")
    private byte[] digitalSignature;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SignatureStatus status;

    @Version
    private Long version;
}