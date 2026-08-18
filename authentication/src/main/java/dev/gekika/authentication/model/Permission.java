package dev.gekika.authentication.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "permissions",
        uniqueConstraints = @UniqueConstraint(name = "uq_permissions_name", columnNames = "name")
)
@Getter
@Setter
@NoArgsConstructor
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Convention: DOMAIN_ACTION, e.g. "PRODUCT_CREATE". Uppercase, unique. */
    @Column(nullable = false, unique = true)
    private String name;

    public Permission(String name) {
        this.name = name;
    }
}