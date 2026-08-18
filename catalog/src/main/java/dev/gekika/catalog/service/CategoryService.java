package dev.gekika.catalog.service;

import dev.gekika.catalog.dto.CategoryRequest;
import dev.gekika.catalog.dto.CategoryResponse;
import dev.gekika.catalog.exception.DuplicateResourceException;
import dev.gekika.catalog.exception.ResourceNotFoundException;
import dev.gekika.catalog.model.Category;
import dev.gekika.catalog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new DuplicateResourceException(
                    "Category '" + request.name() + "' already exists");
        }
        Category category = new Category(request.name(), request.description());
        return toResponse(categoryRepository.save(category));
    }

    // Shared helper: fetch a category or fail clearly. Used by ProductService too.
    @Transactional(readOnly = true)
    public Category getEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category " + id + " not found"));
    }

    private CategoryResponse toResponse(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription());
    }
}