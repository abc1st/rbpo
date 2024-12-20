package ru.mtuci.babok.controller;

import ru.mtuci.babok.model.Product;
import ru.mtuci.babok.request.DataProductRequest;
import ru.mtuci.babok.service.impl.ProductServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manage/product")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ProductController {
    private final ProductServiceImpl productService;

    @PostMapping
    public ResponseEntity<?> save(@RequestBody DataProductRequest request) {
        try {
            Product license = productService.save(request);
            request.setId(license.getId());
            return ResponseEntity.ok(request);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            List<Product> licenses = productService.getAll();
            List<DataProductRequest> data = licenses.stream().map(
                    product -> new DataProductRequest(
                            product.getId(),
                            product.getName(),
                            product.is_blocked()
                    )
            ).toList();
            return ResponseEntity.ok(data);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody DataProductRequest request) {
        try {
            productService.update(request);
            return ResponseEntity.ok(request);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<?> delete(@RequestParam Long id) {
        try {
            productService.delete(id);
            return ResponseEntity.ok("Продукт удалён");
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
