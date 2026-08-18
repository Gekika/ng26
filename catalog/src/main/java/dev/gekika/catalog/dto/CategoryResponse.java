package dev.gekika.catalog.dto;

public record CategoryResponse(
        Long id,
        String name,
        String description
) {}