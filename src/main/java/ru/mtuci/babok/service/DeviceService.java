package ru.mtuci.babok.service;

import ru.mtuci.babok.model.ApplicationUser;
import ru.mtuci.babok.model.Device;
import ru.mtuci.babok.request.DataDeviceRequest;

import java.util.List;
import java.util.Optional;

public interface DeviceService {
    Device registerOrUpdateDevice(String nameDevice, String macDevice, ApplicationUser user);
    Optional<Device> findDeviceByInfo(String name, String mac_address, ApplicationUser user);
    Optional<Device> findDeviceById(Long id);

    // save
    Device save(DataDeviceRequest deviceRequest);

    // read
    List<Device> getAll();

    // update
    Device update(DataDeviceRequest deviceRequest);

    // delete
    void delete(Long id);
}
