package dev.gekika.authentication.repo;

import dev.gekika.authentication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    // Login looks a user up by email.
    Optional<User> findByEmail(String email);

    // Registration checks this to reject duplicate emails up front.
    boolean existsByEmail(String email);
}