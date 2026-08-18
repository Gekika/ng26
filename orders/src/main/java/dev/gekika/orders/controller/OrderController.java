package dev.gekika.orders.controller;

import dev.gekika.orders.dto.OrderResponse;
import dev.gekika.orders.security.AuthenticatedUser;
import dev.gekika.orders.security.CurrentUser;
import dev.gekika.orders.security.RequireRole;
import dev.gekika.orders.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
@RequireRole({"CUSTOMER", "SELLER", "ADMIN"})
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(
            @CurrentUser AuthenticatedUser user,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        // Forward the raw bearer token so Order can read THIS user's cart.
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.checkout(user.id(), authHeader));
    }

    @GetMapping
    public List<OrderResponse> myOrders(@CurrentUser AuthenticatedUser user) {
        return orderService.getUserOrders(user.id());
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@CurrentUser AuthenticatedUser user,
                                  @PathVariable UUID orderId) {
        return orderService.getOrder(user.id(), orderId);
    }
}