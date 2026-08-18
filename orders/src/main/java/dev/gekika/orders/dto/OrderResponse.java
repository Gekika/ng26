package dev.gekika.orders.dto;

import dev.gekika.orders.model.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(UUID id, UUID userId, OrderStatus status,
                            BigDecimal total, List<OrderItemResponse> items, Instant createdAt) {}