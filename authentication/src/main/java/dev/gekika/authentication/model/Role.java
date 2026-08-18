package dev.gekika.authentication.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(
        name = "roles",
        uniqueConstraints = @UniqueConstraint(name = "uq_roles_name", columnNames = "name")
)
@Getter
@Setter
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** e.g. "CUSTOMER", "ADMIN". Uppercase, unique. */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * The permissions this role grants. EAGER because whenever we load a
     * user's roles to build their JWT, we immediately need the permissions
     * too. Join table role_permissions links the two.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    public Role(String name) {
        this.name = name;
    }

    /** Convenience for wiring up permissions when seeding roles. */
    public void addPermission(Permission permission) {
        this.permissions.add(permission);
    }
}
