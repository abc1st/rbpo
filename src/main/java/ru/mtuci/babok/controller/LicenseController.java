package ru.mtuci.babok.controller;

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.hibernate.annotations.ManyToAny;
import ru.mtuci.babok.model.ApplicationUser;
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

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "userId")
    private ApplicationUser user;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "CreatorId")
    private ApplicationUser creator;
}
