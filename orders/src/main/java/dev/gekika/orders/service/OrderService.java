package dev.gekika.orders.service;

import dev.gekika.orders.client.CartClient;
import dev.gekika.orders.client.CatalogClient;
import dev.gekika.orders.dto.OrderItemResponse;
import dev.gekika.orders.dto.OrderResponse;
import dev.gekika.orders.event.OrderPlacedEvent;
import dev.gekika.orders.event.OrderEventPublisher;
import dev.gekika.orders.exception.EmptyCartException;
import dev.gekika.orders.exception.ResourceNotFoundException;
import dev.gekika.orders.model.Order;
import dev.gekika.orders.model.OrderItem;
import dev.gekika.orders.model.OrderStatus;
import dev.gekika.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartClient cartClient;
    private final CatalogClient catalogClient;
    private final PaymentService paymentService;
    private final OrderEventPublisher orderEventPublisher;

    /**
     * THE CHECKOUT SAGA (phase 1, synchronous).
     *
     * @param userId      from the JWT
     * @param bearerToken the raw Authorization header, forwarded to Cart
     */
    @Transactional
    public OrderResponse checkout(UUID userId, String bearerToken) {

        // 1. Read the user's cart (Cart service, token forwarded).
        CartClient.CartView cart = cartClient.getCart(bearerToken);

        if (cart == null || cart.items() == null || cart.items().isEmpty()) {
            throw new EmptyCartException();
        }

        // 2. Build the order, RE-VALIDATING every price against Catalog.
        //    The cart's snapshot might be stale — Catalog is the source of truth.
        Order order = new Order(userId);
        BigDecimal total = BigDecimal.ZERO;

        for (CartClient.CartItemView cartItem : cart.items()) {
            // Live check: product still exists, still active, current price.
            CatalogClient.ProductView product =
                    catalogClient.getProduct(cartItem.productId());

            // Use CATALOG's price, not the cart's snapshot. This is the
            // "price changed at checkout" moment made concrete.
            OrderItem orderItem = new OrderItem(
                    product.id(),
                    product.name(),
                    product.price(),          // authoritative price
                    cartItem.quantity());

            order.addItem(orderItem);
            total = total.add(orderItem.getLineTotal());
        }

        order.setTotal(total);

        // 3. Take payment (stubbed in phase 1 — always succeeds).
        boolean paid = paymentService.charge(userId, total);
        if (!paid) {
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);
            throw new RuntimeException("Payment failed");
        }

        // 4. Mark paid + placed. (Phase 2: publish OrderPaid here.)
        order.setStatus(OrderStatus.PLACED);
        Order saved = orderRepository.save(order);

        // ---- PHASE 2: announce the order to the world ----
        var eventItems = saved.getItems().stream()
                .map(i -> new OrderPlacedEvent.Item(
                        i.getProductId(), i.getProductName(),
                        i.getQuantity(), i.getUnitPrice()))
                .toList();

        orderEventPublisher.publishOrderPlaced(new OrderPlacedEvent(
                saved.getId(), saved.getUserId(), saved.getTotal(),
                eventItems, saved.getCreatedAt()));

        // 5. Empty the cart — the items are now an order (best-effort).
        cartClient.clearCart(bearerToken);

        return toResponse(saved);



    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(UUID userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID userId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + orderId + " not found"));
        // A user can only see their OWN orders — even with a valid id.
        if (!order.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Order " + orderId + " not found");
        }
        return toResponse(order);
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(i -> new OrderItemResponse(i.getProductId(), i.getProductName(),
                        i.getUnitPrice(), i.getQuantity(), i.getLineTotal()))
                .toList();
        return new OrderResponse(order.getId(), order.getUserId(), order.getStatus(),
                order.getTotal(), items, order.getCreatedAt());
    }
}