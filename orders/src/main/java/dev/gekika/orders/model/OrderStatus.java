package dev.gekika.orders.model;

public enum OrderStatus {
    PENDING,     // created, not yet paid
    PAID,        // payment succeeded (stubbed in phase 1)
    PLACED,      // order confirmed to customer
    CONFIRMED,   // phase 2: stock reserved
    SHIPPED,     // phase 2
    DELIVERED,   // phase 2
    CANCELLED,
    FAILED
}