package ru.mtuci.babok.service;


import ru.mtuci.babok.model.ApplicationUser;

public interface AutheneticationService {
    boolean authenticate(ApplicationUser user, String password);
}
