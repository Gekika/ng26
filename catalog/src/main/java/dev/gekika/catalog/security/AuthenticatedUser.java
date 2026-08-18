package dev.gekika.catalog.security;

import java.util.List;
import java.util.UUID;

public record AuthenticatedUser(
        UUID id,
        String email,
        List<String> roles
) {
    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}