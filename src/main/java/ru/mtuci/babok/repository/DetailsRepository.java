package ru.mtuci.babok.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mtuci.babok.model.Details;

@Repository
public interface DetailsRepository extends JpaRepository<Details, Long> {
}
