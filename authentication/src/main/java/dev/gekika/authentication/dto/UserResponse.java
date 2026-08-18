package dev.gekika.authentication.dto;


import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        Set<String> roles
) {}
