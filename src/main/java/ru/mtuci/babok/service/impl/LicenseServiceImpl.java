package ru.mtuci.babok.service.impl;

import ru.mtuci.babok.exceptions.categories.License.LicenseErrorActivationException;
import ru.mtuci.babok.exceptions.categories.License.LicenseNotFoundException;
import ru.mtuci.babok.exceptions.categories.LicenseTypeNotFoundException;
import ru.mtuci.babok.exceptions.categories.ProductNotFoundException;
import ru.mtuci.babok.exceptions.categories.UserNotFoundException;
import ru.mtuci.babok.model.*;
import ru.mtuci.babok.repository.DeviceLicenseRepository;
import ru.mtuci.babok.repository.LicenseRepository;
import ru.mtuci.babok.request.DataLicenseRequest;
import ru.mtuci.babok.service.LicenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.text.SimpleDateFormat;

import java.sql.Date;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LicenseServiceImpl implements LicenseService {
    private final LicenseRepository licenseRepository;
    private final ProductServiceImpl productService;
    private final UserServiceImpl userService;
    private final LicenseTypeServiceImpl licenseTypeService;
    private final LicenseHistoryServiceImpl licenseHistoryService;
    private final DeviceLicenseServiceImpl deviceLicenseService;
    private final DeviceLicenseRepository deviceLicenseRepository;


    @Override
    public License createLicense(
            Long productId, Long ownerId, Long licenseTypeId,
            Integer device_count, Long duration) {
        // Проверка входных параметров
        if (productId == null || ownerId == null || licenseTypeId == null) {
            throw new IllegalArgumentException("Необходимо указать все параметры: продукт, владелец и тип лицензии");
        }

        // Валидация входных данных с информативными сообщениями об ошибках
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Продукт с ID " + productId + " не найден"));

        if (product.is_blocked()) {
            throw new IllegalStateException("Невозможно создать лицензию для заблокированного продукта");
        }

        ApplicationUser owner = userService.getUserById(ownerId)
                .orElseThrow(() -> new UserNotFoundException("Владелец с ID " + ownerId + " не найден"));

        LicenseType licenseType = licenseTypeService.getLicenseTypeById(licenseTypeId)
                .orElseThrow(() -> new LicenseTypeNotFoundException("Тип лицензии с ID " + licenseTypeId + " не найден"));

        // Проверка количества устройств
        if (device_count <= 0) {
            throw new IllegalArgumentException("Количество устройств должно быть положительным");
        }

        // Определение длительности лицензии
        Long effectiveDuration = (duration == null || duration == 0)
                ? licenseType.getDefault_duration()
                : duration;

        // Создание новой лицензии с расширенной логикой
        License license = new License();
        license.setProduct(product);
        license.setOwner(owner);
        license.setLicenseType(licenseType);
        license.setDevice_count(device_count);
        license.setBlocked(false);
        license.setUser(null);
        license.setFirst_activation_date(null);
        license.setDuration(effectiveDuration);

        // Расчет даты окончания
        Date endDate = new Date(System.currentTimeMillis() + effectiveDuration * 1000);
        license.setEnding_date(endDate);

        // Генерация уникального кода лицензии с дополнительной защитой
        String code = generateSecureCodeLicense(productId, ownerId, licenseTypeId, device_count);
        license.setCode(code);

        // Формирование подробного описания лицензии
        String description = buildLicenseDescription(license);
        license.setDescription(description);

        // Сохранение лицензии
        license = licenseRepository.save(license);

        // Запись истории создания лицензии
        licenseHistoryService.recordLicenseChange(
                license,
                owner,
                LicenseHistoryStatus.CREATE.name(),
                description
        );

        return license;
    }

    private String generateSecureCodeLicense(Long productId, Long ownerId, Long licenseTypeId, Integer device_count) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawCode = productId.toString() +
                ownerId.toString() +
                licenseTypeId.toString() +
                device_count.toString() +
                System.currentTimeMillis();
        return encoder.encode(rawCode);
    }

    private String buildLicenseDescription(License license) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        return String.format(
                "Лицензия:\n" +
                "- Продукт: %s\n" +
                "- Тип: %s\n"+
                "- Владелец: %s\n" +
                "- Количество устройств: %d\n" +
                "- Создана: %s\n" +
                "- Действует до: %s\n" + "- Длительность: %d сек",
                license.getProduct().getName(),
                license.getLicenseType().getName(),
                license.getOwner().getLogin(),
                license.getDevice_count(),
                formatter.format(new Date(System.currentTimeMillis())),
                formatter.format(license.getEnding_date()),
                license.getDuration()
        );
    }


    @Override
    public Ticket activateLicense(String activationCode, Device device, ApplicationUser user) {
        // Поиск лицензии по коду активации с детальной обработкой
        License license = licenseRepository.findByCode(activationCode)
                .orElseThrow(() -> new LicenseNotFoundException("Лицензия не найдена"));

        // Расширенная валидация лицензии с подробным описанием причин отказа
        ValidationResult validationResult = validateLicenseActivation(license, device, user);

        if (!validationResult.isValid()) {
            // Логирование неудачной попытки активации
            licenseHistoryService.recordLicenseChange(
                    license,
                    user,
                    LicenseHistoryStatus.ERROR.name(),
                    validationResult.getErrorMessage()
            );
            throw new LicenseErrorActivationException(validationResult.getErrorMessage());
        }

        // Первичная активация лицензии
        if (license.getUser() == null) {
            license.setUser(user);
            license.setFirst_activation_date(new Date(System.currentTimeMillis()));
        }

        // Обновление лицензии
        updateLicense(license);

        // Создание связи устройство-лицензия
        createDeviceLicense(license, device);

        // Запись истории активации
        licenseHistoryService.recordLicenseChange(
                license,
                user,
                LicenseHistoryStatus.ACTIVATE.name(),
                "Лицензия успешно активирована на устройстве " + device.getName()
        );

        // Генерация билета
        return generateTicket(license, device, "Лицензия успешно активирована");
    }

    private ValidationResult validateLicenseActivation(License license, Device device, ApplicationUser user) {
        // Проверка блокировки лицензии
        if (license.isBlocked()) {
            return ValidationResult.invalid("Лицензия заблокирована");
        }

        // Проверка даты окончания лицензии
        Date currentDate = new Date(System.currentTimeMillis());
        if (license.getEnding_date().before(currentDate)) {
            return ValidationResult.invalid("Срок действия лицензии истек");
        }

        // Проверка пользователя
        if (license.getUser() != null && !license.getUser().getId().equals(user.getId())) {
            return ValidationResult.invalid("Лицензия принадлежит другому пользователю");
        }

        // Проверка количества устройств
        long activeDevicesCount = license.getDeviceLicenses().size();
        if (activeDevicesCount >= license.getDevice_count()) {
            return ValidationResult.invalid("Достигнуто максимальное количество устройств");
        }

        // Проверка уникальности устройства
        boolean deviceAlreadyActivated = license.getDeviceLicenses().stream()
                .anyMatch(dl -> dl.getDevice().getId().equals(device.getId()));

        if (deviceAlreadyActivated) {
            return ValidationResult.invalid("Устройство уже активировано для этой лицензии");
        }

        return ValidationResult.valid();
    }

    @Override
    public boolean validateLicense(License license, Device device, ApplicationUser user) {
        ValidationResult validationResult = validateLicenseActivation(license, device, user);
        return validationResult.isValid();
    }

    @Override
    public void createDeviceLicense(License license, Device device) {
        Optional<DeviceLicense> existingDeviceLicense = deviceLicenseRepository.findByDeviceAndLicense(device, license);

        if (existingDeviceLicense.isPresent()) {
            throw new IllegalStateException("Устройство уже имеет активацию для этой лицензии");
        }

        // Создание новой связи
        DeviceLicense deviceLicense = new DeviceLicense();
        deviceLicense.setDevice(device);
        deviceLicense.setLicense(license);
        deviceLicense.setActivation_date(new Date(System.currentTimeMillis()));

        deviceLicenseService.saveDeviceLicense(deviceLicense);
    }

    @Override
    public void updateLicense(License license) {
        // Обновление даты первой активации, если еще не установлена
        if (license.getFirst_activation_date() == null) {
            license.setFirst_activation_date(new Date(System.currentTimeMillis()));
        }

        // Формирование подробного описания
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        String updateDescription = String.format(
                "Лицензия обновлена:\n" +
                        "Пользователь: %s\n" +
                        "Первая активация: %s\n" +
                        "Активированных устройств: %d\n" +
                        "Осталось активаций: %d",
                license.getUser().getLogin(),
                formatter.format(license.getFirst_activation_date()),
                license.getDeviceLicenses().size(),
                license.getDevice_count() - license.getDeviceLicenses().size()
        );

        license.setDescription(updateDescription);

        // Сохранение обновленной лицензии
        licenseRepository.save(license);

        // Запись изменений в историю
        licenseHistoryService.recordLicenseChange(
                license,
                license.getUser(),
                LicenseHistoryStatus.MODIFICATION.name(),
                updateDescription
        );
    }

    @Override
    public List<License> getActiveLicensesForDevice(Device device, ApplicationUser user) {
        return device.getDeviceLicenses().stream()
                .map(DeviceLicense::getLicense)
                .filter(license ->
                        license.getUser().getId().equals(user.getId()) &&
                                !license.isBlocked() &&
                                license.getEnding_date().after(new Date(System.currentTimeMillis()))
                )
                .collect(Collectors.toList());
    }

    private License edit(License license, DataLicenseRequest request) {
        license.setLicenseType(licenseTypeService.getLicenseTypeById(request.getType_id()).orElseThrow(
                () -> new LicenseTypeNotFoundException("Тип лицензии не найден")
        ));
        license.setProduct(productService.getProductById(request.getProduct_id()).orElseThrow(
                () -> new ProductNotFoundException("Продукт не найден")
        ));
        license.setUser(userService.getUserById(request.getUser_id()).orElseThrow(
                () -> new UsernameNotFoundException("Пользователь не найден")
        ));
        license.setOwner(userService.getUserById(request.getOwner_id()).orElseThrow(
                () -> new UsernameNotFoundException("Владелец не найден")
        ));
        license.setFirst_activation_date(request.getFirst_activation_date());
        license.setEnding_date(request.getEnding_date());
        license.setBlocked(request.isBlocked());
        license.setDevice_count(request.getDevice_count());
        license.setDuration(request.getDuration());
        license.setDescription(request.getDescription());
        return license;
    }

    @Override
    public License save(DataLicenseRequest request) {
        return licenseRepository.save(edit(new License(), request));
    }

    @Override
    public List<License> getAll() {
        return licenseRepository.findAll();
    }

    @Override
    public License update(DataLicenseRequest request) {
        License license = licenseRepository.findById(request.getId()).orElseThrow(
                () -> new LicenseNotFoundException("Лицензия не найдена")
        );
        return licenseRepository.save(edit(license, request));
    }

    @Override
    public void delete(Long id) {
        licenseRepository.deleteById(id);
    }

    @Override
    public Ticket generateTicket(License license, Device device, String description) {
        Ticket ticket = new Ticket();
        ticket.setNowDate(new Date(System.currentTimeMillis()));
        ticket.setActivationDate(license.getFirst_activation_date());
        ticket.setExpirationDate(license.getEnding_date());
        ticket.setExpiration(license.getDuration());
        ticket.setUserID(license.getUser().getId());
        ticket.setDeviceID(device.getId());
        ticket.setBlockedLicence(license.isBlocked());
        ticket.setDescription(description);

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String ds = bCryptPasswordEncoder.encode(
                ticket.getNowDate().toString() + ticket.getActivationDate().toString() +
                        ticket.getExpirationDate().toString() + ticket.getExpiration().toString() +
                        ticket.getUserID().toString() + ticket.getDeviceID().toString()
        );
        ticket.setDigitalSignature(ds);
        return ticket;
    }

    @Override
    public List<Ticket> licenseRenewal(String activationCode, ApplicationUser user) {
        // Получаем лицензию
        License license = licenseRepository.findByCode(activationCode)
                .orElseThrow(() -> new LicenseNotFoundException("Ключ лицензии недействителен"));

        // Создаем список билетов для каждого устройства
        List<Ticket> tickets = license.getDeviceLicenses().stream()
                .map(deviceLicense -> generateTicket(license, deviceLicense.getDevice(), ""))
                .collect(Collectors.toList());

        // Расширенные проверки возможности продления
        ValidationResult validationResult = validateLicenseRenewal(license, user);

        if (!validationResult.isValid()) {
            // Обработка невозможности продления
            tickets.forEach(ticket -> {
                ticket.setDescription(validationResult.getErrorMessage());
                licenseHistoryService.recordLicenseChange(
                        license,
                        user,
                        LicenseHistoryStatus.ERROR.name(),
                        validationResult.getErrorMessage()
                );
            });
            return tickets;
        }

        // Продление лицензии
        extendLicense(license, user);

        // Обновление билетов и история
        tickets.forEach(ticket -> {
            ticket.setDescription("Лицензия успешно продлена");
            ticket.setExpirationDate(license.getEnding_date());
            ticket.setExpiration(license.getDuration());

            licenseHistoryService.recordLicenseChange(
                    license,
                    user,
                    LicenseHistoryStatus.MODIFICATION.name(),
                    "Лицензия продлена на стандартный период"
            );
        });

        return tickets;
    }

    // Метод валидации продления лицензии
    private ValidationResult validateLicenseRenewal(License license, ApplicationUser user) {
        // Проверка блокировки
        if (license.isBlocked()) {
            return ValidationResult.invalid("Лицензия заблокирована");
        }

        // Проверка пользователя
        if (license.getUser() != null && !license.getUser().getId().equals(user.getId())) {
            return ValidationResult.invalid("Недостаточно прав для продления");
        }

        // Дополнительные проверки
        Date currentDate = new Date(System.currentTimeMillis());

        // Максимальная длительность лицензии (5 лет)
        long maxLicenseDuration = 5L * 365 * 24 * 60 * 60 * 1000; // 5 лет в миллисекундах

        // Если текущая длительность + новый период превышает максимальную
        long newEndTime = license.getEnding_date().getTime() +
                license.getLicenseType().getDefault_duration() * 1000;

        if (newEndTime - currentDate.getTime() > maxLicenseDuration) {
            return ValidationResult.invalid("Превышена максимальная длительность лицензии");
        }

        // Если лицензия просрочена более чем на 30 дней
        long thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000;
        if (license.getEnding_date().getTime() + thirtyDaysInMillis < currentDate.getTime()) {
            return ValidationResult.invalid("Лицензия просрочена более чем на 30 дней");
        }

        return ValidationResult.valid();
    }

    // Метод продления лицензии
    private void extendLicense(License license, ApplicationUser user) {
        // Получаем стандартную длительность из типа лицензии
        Long defaultDuration = license.getLicenseType().getDefault_duration();

        // Расчет новой даты окончания
        Date currentDate = new Date(System.currentTimeMillis());
        Date newEndDate = license.getEnding_date().before(currentDate)
                ? new Date(currentDate.getTime() + defaultDuration * 1000)
                : new Date(license.getEnding_date().getTime() + defaultDuration * 1000);

        // Обновляем параметры лицензии
        license.setEnding_date(newEndDate);
        license.setDuration(license.getDuration() + defaultDuration);

        // Если пользователь не установлен, устанавливаем
        if (license.getUser() == null) {
            license.setUser(user);
        }

        // Сохраняем лицензию
        licenseRepository.save(license);
    }

}
