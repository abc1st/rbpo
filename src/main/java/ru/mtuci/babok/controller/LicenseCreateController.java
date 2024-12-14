package ru.mtuci.babok.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mtuci.babok.model.LicenseActivate;
import ru.mtuci.babok.request.LicenseCreateRequest;
import ru.mtuci.babok.request.LicenseRequest;
import ru.mtuci.babok.service.impl.LicenseServiceImpl;

@RestController
@RequestMapping("/admin/licenseCreate")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class LicenseCreateController {
    private final LicenseServiceImpl licenseService;

    @PostMapping
    public ResponseEntity<?> LicenseCreate(@RequestBody LicenseCreateRequest licenseCreateRequest)
    {
        LicenseActivate licenseActivate = licenseService.createLicense(
                licenseCreateRequest.getCreatorId(),
                licenseCreateRequest.getDevice_count(),
                licenseCreateRequest.getCurrent_device(),
                licenseCreateRequest.getlifeTime()
        );
        return ResponseEntity.ok(licenseActivate);
    }
}
