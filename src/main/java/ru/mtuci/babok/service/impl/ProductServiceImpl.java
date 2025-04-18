package ru.mtuci.babok.service.impl;

import ru.mtuci.babok.exceptions.categories.ProductNotFoundException;
import ru.mtuci.babok.model.Product;
import ru.mtuci.babok.repository.ProductRepository;
import ru.mtuci.babok.request.DataProductRequest;
import ru.mtuci.babok.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    private Product edit(Product product, DataProductRequest request) {
        product.setName(request.getName());
        product.set_blocked(request.is_blocked());
        return product;
    }

    @Override
    public Product save(DataProductRequest request) {
        return productRepository.save(edit(new Product(), request));
    }

    @Override
    public List<Product> getAll() {
        return productRepository.findAll();
    }

    @Override
    public Product update(DataProductRequest request) {
        Product product = productRepository.findById(request.getId()).orElseThrow(
                () -> new ProductNotFoundException("Продукт не найден")
        );
        return productRepository.save(edit(product, request));
    }

    @Override
    public void delete(Long id) {
        productRepository.deleteById(id);
    }
}
