package ru.mtuci.babok.repository;

import ru.mtuci.babok.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}