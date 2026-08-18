package dev.gekika.catalog.controller;

import dev.gekika.catalog.dto.ProductRequest;
import dev.gekika.catalog.dto.ProductResponse;
import dev.gekika.catalog.security.RequireRole;
import dev.gekika.catalog.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/catalog/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ---- Public reads ----

    @GetMapping
    public List<ProductResponse> list(@RequestParam(required = false) Long categoryId,
                                      @RequestParam(required = false) String search) {
        if (categoryId != null) return productService.findByCategory(categoryId);
        if (search != null)     return productService.search(search);
        return productService.findAllActive();
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable UUID id) {
        return productService.findById(id);
    }

    // ---- Guarded writes: SELLER or ADMIN ----

    @RequireRole({"SELLER", "ADMIN"})
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.create(request));
    }

    @RequireRole({"SELLER", "ADMIN"})
    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable UUID id,
                                  @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @RequireRole({"SELLER", "ADMIN"})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}