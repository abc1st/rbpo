package ru.mtuci.babok.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.babok.configuration.JwtTokenProvider;
import ru.mtuci.babok.model.SignatureAudit;
import ru.mtuci.babok.model.SignatureEntity;
import ru.mtuci.babok.model.SignatureStatus;
import ru.mtuci.babok.service.SignatureManagementService;
import ru.mtuci.babok.service.SignatureService;

import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/signatures")
@RequiredArgsConstructor
public class SignatureController {
    private final SignatureManagementService service;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final SignatureService signatureService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SignatureEntity> createSignature(
            @RequestHeader("Authorization") String auth,
            @RequestBody SignatureEntity entity) throws Exception {
        String login = jwtTokenProvider.getUsername(auth.split(" ")[1]);
        return ResponseEntity.ok(service.createSignature(entity, login));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SignatureEntity> updateSignature(
            @RequestHeader("Authorization") String auth,
            @PathVariable UUID id,
            @RequestBody SignatureEntity entity) throws Exception {
        String login = jwtTokenProvider.getUsername(auth.split(" ")[1]);
        return ResponseEntity.ok(service.updateSignature(id, entity, login));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSignature(
            @RequestHeader("Authorization") String auth,
            @PathVariable UUID id) throws Exception {
        String login = jwtTokenProvider.getUsername(auth.split(" ")[1]);
        service.deleteSignature(id, login);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<SignatureEntity>> getAllActualSignatures() {
        return ResponseEntity.ok(service.getAllActualSignatures());
    }

    @GetMapping("/diff")
    public ResponseEntity<List<SignatureEntity>> getSignaturesUpdatedAfter(@RequestParam String since) {
        LocalDateTime sinceDate = LocalDateTime.parse(since);
        return ResponseEntity.ok(service.getSignaturesUpdatedAfter(sinceDate));
    }

    @PostMapping("/by-ids")
    public ResponseEntity<List<SignatureEntity>> getSignaturesByIds(@RequestBody List<UUID> ids) {
        return ResponseEntity.ok(service.getSignaturesByIds(ids));
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SignatureEntity>> getSignaturesByStatus(@RequestParam String status) {
        SignatureStatus signatureStatus = SignatureStatus.valueOf(status.toUpperCase());
        return ResponseEntity.ok(service.getSignaturesByStatus(signatureStatus));
    }

    @GetMapping("/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SignatureAudit>> getAuditRecords(
            @RequestParam(required = false) String changeType,
            @RequestParam(required = false) String changedBy,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {

        if (changeType != null) {
            return ResponseEntity.ok(service.getAuditRecordsByChangeType(changeType));
        } else if (changedBy != null) {
            return ResponseEntity.ok(service.getAuditRecordsByChangedBy(changedBy));
        } else if (start != null && end != null) {
            try {
                LocalDateTime startDate = LocalDateTime.parse(start);
                LocalDateTime endDate = LocalDateTime.parse(end);
                return ResponseEntity.ok(service.getAuditRecordsByDateRange(startDate, endDate));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(null); // Ошибка формата даты
            }
        } else {
            return ResponseEntity.ok(service.getAllAuditRecords());
        }
    }

    @GetMapping("/{id}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SignatureAudit>> getAuditRecordsBySignatureId(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getAuditRecordsBySignatureId(id));
    }


    @GetMapping(value = "/download", produces = MediaType.MULTIPART_MIXED_VALUE)
    public ResponseEntity<MultiValueMap<String, Object>> downloadSignatures(
            @RequestParam(required = false) String since) throws Exception {

        List<SignatureEntity> signatures;
        if (since != null) {
            LocalDateTime sinceDate = LocalDateTime.parse(since);
            signatures = service.getSignaturesUpdatedAfter(sinceDate);
        } else {
            signatures = service.getAllActualSignatures();
        }

        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        ByteArrayOutputStream metaStream = new ByteArrayOutputStream();

        for (SignatureEntity signature : signatures) {
            byte[] serialized = service.serializeSignatureFields(signature);
            dataStream.write(serialized);

            String idStr = signature.getId().toString();
            byte[] digitalSig = signature.getDigitalSignature();
            if (digitalSig == null) digitalSig = new byte[0];

            metaStream.write(idStr.getBytes(StandardCharsets.UTF_8));
            metaStream.write(":".getBytes(StandardCharsets.UTF_8));
            metaStream.write(digitalSig);
        }

        byte[] data = dataStream.toByteArray();
        byte[] massiveSignature = metaStream.toByteArray();
        byte[] countSignature = ByteBuffer.allocate(4).putInt(signatures.size()).array();
        byte[] manifestSignature = service.generateManifestSignature(signatures.size(), massiveSignature);

        ByteArrayOutputStream manifestStream = new ByteArrayOutputStream();
        manifestStream.write(countSignature);
        manifestStream.write(massiveSignature);
        manifestStream.write(manifestSignature);
        byte[] manifest = manifestStream.toByteArray();
        return buildMultipartResponse(manifest, data);
    }

    private ResponseEntity<MultiValueMap<String, Object>> buildMultipartResponse(byte[] manifest, byte[] data) {
        ByteArrayResource manifestRes = new ByteArrayResource(manifest) {
            @Override
            public String getFilename() {
                return "manifest.bin";
            }
        };

        ByteArrayResource dataRes = new ByteArrayResource(data) {
            @Override
            public String getFilename() {
                return "data.bin";
            }
        };

        LinkedMultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("manifest", new HttpEntity<>(manifestRes, createHeaders("manifest.bin")));
        parts.add("data", new HttpEntity<>(dataRes, createHeaders("data.bin")));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(MediaType.MULTIPART_MIXED_VALUE))
                .body(parts);
    }

    private HttpHeaders createHeaders(String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return headers;
    }

}