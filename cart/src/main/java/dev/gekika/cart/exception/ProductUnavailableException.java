package dev.gekika.cart.exception;

import java.util.UUID;

public class ProductUnavailableException extends RuntimeException {
    public ProductUnavailableException(UUID productId) {
        super("Product " + productId + " is unavailable or does not exist");
    }
}