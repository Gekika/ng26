package dev.gekika.cart.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A user's shopping cart. One cart per user (enforced by unique user_id).
 * Owns its items — deleting the cart deletes them.
 */
@Entity
@Table(name = "carts",
        uniqueConstraints = @UniqueConstraint(name = "uq_carts_user", columnNames = "user_id"))
@Getter
@Setter
@NoArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** The user who owns this cart — taken from the JWT, not the URL. */
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    /**
     * The items. cascade + orphanRemoval means items live and die with
     * the cart, and removing an item from this list deletes its row.
     */
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Cart(UUID userId) {
        this.userId = userId;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Helpers keep the bidirectional link consistent.
    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this);
    }

    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
    }
}