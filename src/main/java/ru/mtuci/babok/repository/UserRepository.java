package ru.mtuci.babok.repository;

import ru.mtuci.babok.model.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<ApplicationUser, Long> {
    Optional<ApplicationUser> findByLogin(String login);
    Optional<ApplicationUser> findByEmail(String email);
}
