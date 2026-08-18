package dev.gekika.cart.service;

import dev.gekika.cart.client.CatalogClient;
import dev.gekika.cart.dto.*;
import dev.gekika.cart.exception.ResourceNotFoundException;
import dev.gekika.cart.model.Cart;
import dev.gekika.cart.model.CartItem;
import dev.gekika.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CatalogClient catalogClient;

    /** Get the user's cart, creating an empty one if they don't have it yet. */
    @Transactional
    public CartResponse getCart(UUID userId) {
        Cart cart = getOrCreateCart(userId);
        return toResponse(cart);
    }

    /**
     * Add an item. This is where the synchronous Catalog call happens:
     * we validate the product exists/active AND snapshot its price + name.
     */
    @Transactional
    public CartResponse addItem(UUID userId, AddItemRequest request) {
        Cart cart = getOrCreateCart(userId);

        // ---- The cross-service call: validate + price the product ----
        CatalogClient.ProductView product = catalogClient.getProduct(request.productId());

        // If the product is already in the cart, bump quantity instead of duplicating.
        CartItem existing = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(request.productId()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + request.quantity());
            // Refresh the snapshot to the current price on re-add.
            existing.setUnitPrice(product.price());
            existing.setProductName(product.name());
        } else {
            CartItem item = new CartItem(
                    product.id(),
                    product.name(),      // snapshot
                    product.price(),     // snapshot
                    request.quantity());
            cart.addItem(item);
        }

        cartRepository.save(cart);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse updateQuantity(UUID userId, UUID itemId, UpdateQuantityRequest request) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item " + itemId + " not in cart"));

        item.setQuantity(request.quantity());
        cartRepository.save(cart);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(UUID userId, UUID itemId) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item " + itemId + " not in cart"));

        cart.removeItem(item);
        cartRepository.save(cart);
        return toResponse(cart);
    }

    @Transactional
    public void clearCart(UUID userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();   // orphanRemoval deletes the rows
        cartRepository.save(cart);
    }

    // ---- helpers ----

    private Cart getOrCreateCart(UUID userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(new Cart(userId)));
    }

    private CartResponse toResponse(Cart cart) {
        var items = cart.getItems().stream()
                .map(i -> new CartItemResponse(
                        i.getId(), i.getProductId(), i.getProductName(),
                        i.getUnitPrice(), i.getQuantity(), i.getLineTotal()))
                .toList();

        BigDecimal total = items.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), cart.getUserId(), items, total);
    }
}