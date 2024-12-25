package ru.mtuci.babok.service.impl;

import ru.mtuci.babok.exceptions.categories.DeviceNotFoundException;
import ru.mtuci.babok.exceptions.categories.UserNotFoundException;
import ru.mtuci.babok.model.ApplicationUser;
import ru.mtuci.babok.model.Device;
import ru.mtuci.babok.repository.DeviceRepository;
import ru.mtuci.babok.request.DataDeviceRequest;
import ru.mtuci.babok.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {
    private final DeviceRepository deviceRepository;
    private final UserServiceImpl userServiceImpl;



    @Override
    public Device registerOrUpdateDevice(String nameDevice, String macDevice, ApplicationUser user) {
        Device device = deviceRepository.findByMacAddress(macDevice).orElse(null);

        if (device == null || !device.getUser().getId().equals(user.getId())) {
            device = new Device();
            device.setName(nameDevice);
            device.setMacAddress(macDevice);
            device.setUser(user);
        }
        else if (!nameDevice.equals(device.getName())) {    // поменялось название устройства
            device.setName(nameDevice);
        }

        return deviceRepository.save(device);
    }

    @Override
    public Optional<Device> findDeviceByInfo(String name, String mac_address, ApplicationUser user) {
        return deviceRepository.findByNameAndMacAddressAndUser(name, mac_address, user);
    }

    @Override
    public Optional<Device> findDeviceById(Long id) {
        return deviceRepository.findById(id);
    }

    private Device edit(Device device, DataDeviceRequest deviceRequest) {
        device.setName(deviceRequest.getName());
        device.setMacAddress(deviceRequest.getMacAddress());
        device.setUser(userServiceImpl.getUserById(deviceRequest.getUser_id()).orElseThrow(
                () -> new UserNotFoundException("Пользователь не найден")
        ));
        return device;
    }

    @Override
    public Device save(DataDeviceRequest deviceRequest) {
        Device device = edit(new Device(), deviceRequest);
        return deviceRepository.save(device);
    }

    @Override
    public List<Device> getAll() {
        return deviceRepository.findAll();
    }

    @Override
    public Device update(DataDeviceRequest deviceRequest) {
        Device device = deviceRepository.findById(deviceRequest.getId()).orElseThrow(
                () -> new DeviceNotFoundException("Устройство не найдено")
        );
        return deviceRepository.save(edit(device, deviceRequest));
    }

    @Override
    public void delete(Long id) {
        deviceRepository.deleteById(id);
    }
}
