package dev.gekika.catalog.controller;

import dev.gekika.catalog.dto.CategoryRequest;
import dev.gekika.catalog.dto.CategoryResponse;
import dev.gekika.catalog.security.RequireRole;
import dev.gekika.catalog.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/catalog/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // Public — anyone can browse categories.
    @GetMapping
    public List<CategoryResponse> list() {
        return categoryService.findAll();
    }

    // Guarded — only SELLER or ADMIN can create categories.
    @RequireRole({"SELLER", "ADMIN"})
    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.create(request));
    }
}