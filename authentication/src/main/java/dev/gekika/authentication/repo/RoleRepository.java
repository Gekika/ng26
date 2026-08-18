package dev.gekika.authentication.repo;


import dev.gekika.authentication.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    // Registration uses this to fetch the CUSTOMER role and attach it
    // to the new user. Returns Optional so "role missing" is explicit
    // rather than a null surprise.
    Optional<Role> findByName(String name);
}