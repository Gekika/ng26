package dev.gekika.catalog.repository;

import dev.gekika.catalog.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    // Browse products in a category.
    List<Product> findByCategoryId(Long categoryId);

    // Only show active products to customers (soft-delete friendly).
    List<Product> findByActiveTrue();

    // Simple name search — case-insensitive contains.
    List<Product> findByNameContainingIgnoreCase(String name);
}