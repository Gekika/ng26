package dev.gekika.orders.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cart")
public record CartProperties(String baseUrl) {}