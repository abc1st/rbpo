package ru.mtuci.babok.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.babok.model.SignatureHistory;

public interface SignatureHistoryRepository extends JpaRepository<SignatureHistory, Long> {
}