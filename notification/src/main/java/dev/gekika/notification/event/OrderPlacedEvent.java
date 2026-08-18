package dev.gekika.notification.event;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Published when an order is placed. This is the CONTRACT between Order
 * and any consumer. Keep it stable — consumers depend on this shape.
 */
public record OrderPlacedEvent(
        UUID orderId,
        UUID userId,
        BigDecimal total,
        List<Item> items,
        Instant placedAt
) {
    public record Item(UUID productId, String productName, int quantity, BigDecimal unitPrice) {}
}