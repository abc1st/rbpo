package ru.mtuci.babok.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.babok.configuration.JwtTokenProvider;
import ru.mtuci.babok.model.SignatureEntity;
import ru.mtuci.babok.model.SignatureStatus;
import ru.mtuci.babok.service.impl.SignatureManagementServiceImpl;
import ru.mtuci.babok.service.impl.SignatureServiceImpl;

import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/signatures")
@RequiredArgsConstructor
public class SignatureController {
    private final SignatureManagementServiceImpl service;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final SignatureServiceImpl signatureService;

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

    @GetMapping("/download")
    public void downloadSignatures(@RequestParam(required = false) String since, HttpServletResponse response) throws Exception {

    }

}