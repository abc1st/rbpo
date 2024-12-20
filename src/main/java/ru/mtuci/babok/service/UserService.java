package ru.mtuci.babok.service;


import ru.mtuci.babok.model.ApplicationUser;
import ru.mtuci.babok.request.DataUserRequest;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<ApplicationUser> getUserById(Long id);
    Optional<ApplicationUser> getUserByLogin(String login);

    // save
    ApplicationUser save(DataUserRequest request);

    // read
    List<ApplicationUser> getAll();

    // update
    ApplicationUser update(DataUserRequest request);

    // delete
    void delete(Long id);
}
