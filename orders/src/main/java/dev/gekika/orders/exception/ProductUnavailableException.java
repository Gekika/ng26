package dev.gekika.orders.exception;
import java.util.UUID;

public class ProductUnavailableException extends RuntimeException {
    public ProductUnavailableException(UUID id) {
        super("Product " + id + " is unavailable");
    }
}
