package dev.gekika.notification.event;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.events")
public record EventProperties(String exchange, String orderPlacedQueue, String orderPlacedRoutingKey) {}