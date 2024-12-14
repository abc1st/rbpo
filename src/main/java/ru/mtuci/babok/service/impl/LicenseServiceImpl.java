package ru.mtuci.babok.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ru.mtuci.babok.model.LicenseActivate;
import ru.mtuci.babok.repository.LicenseActivateRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LicenseServiceImpl {
    private final LicenseActivateRepository licenseActivateRepository;

    private String generateLicenseCode(Long CreatorId, Integer device_count, String current_device)
    {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(CreatorId.toString() + device_count.toString() + current_device);
    }

    @Override
    public LicenseActivate createLicense(
            Long CreatorId, Integer device_count, String  current_device, Integer lifeTime
    ){
        LicenseActivate licenseActivate = new LicenseActivate();

        licenseActivate.setDevice_count(device_count);
        licenseActivate.setCurrent_device(current_device);
        licenseActivate.set_active(true);
        licenseActivate.setUser(null);

        licenseActivate.setActivation_date(null);

        String code = generateLicenseCode(CreatorId, device_count, current_device);
        licenseActivate.setActivation_code(code);

        licenseActivate.setLifeTime(lifeTime);

        LocalDateTime endDateTime = LocalDateTime.now().plusMonths(lifeTime);
        licenseActivate.setEnd_date(endDateTime);

        licenseActivate = licenseActivateRepository.save(licenseActivate);
        return licenseActivate;
    }
}

