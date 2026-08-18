package dev.gekika.cart.controller;

import dev.gekika.cart.dto.*;
import dev.gekika.cart.security.AuthenticatedUser;
import dev.gekika.cart.security.CurrentUser;
import dev.gekika.cart.security.RequireRole;
import dev.gekika.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/cart")
@RequiredArgsConstructor
// Every cart endpoint requires a logged-in user. Any authenticated role
// can have a cart, so we require the base CUSTOMER role.
@RequireRole({"CUSTOMER", "SELLER", "ADMIN"})
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponse getCart(@CurrentUser AuthenticatedUser user) {
        return cartService.getCart(user.id());
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@CurrentUser AuthenticatedUser user,
                                                @Valid @RequestBody AddItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartService.addItem(user.id(), request));
    }

    @PutMapping("/items/{itemId}")
    public CartResponse updateQuantity(@CurrentUser AuthenticatedUser user,
                                       @PathVariable UUID itemId,
                                       @Valid @RequestBody UpdateQuantityRequest request) {
        return cartService.updateQuantity(user.id(), itemId, request);
    }

    @DeleteMapping("/items/{itemId}")
    public CartResponse removeItem(@CurrentUser AuthenticatedUser user,
                                   @PathVariable UUID itemId) {
        return cartService.removeItem(user.id(), itemId);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@CurrentUser AuthenticatedUser user) {
        cartService.clearCart(user.id());
        return ResponseEntity.noContent().build();
    }
}