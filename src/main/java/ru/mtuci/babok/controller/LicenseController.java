package ru.mtuci.babok.controller;

import ru.mtuci.babok.service.impl.LicenseServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("*admin/license")
@RequiredArgsConstructor
@PreAuthorize("*hasRole('ADMIN')")
public class LicenseController {
    private final LicenseServiceImpl licenseService;
}
