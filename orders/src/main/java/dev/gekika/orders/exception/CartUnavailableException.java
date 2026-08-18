package dev.gekika.orders.exception;

public class CartUnavailableException extends RuntimeException {
    public CartUnavailableException(String m) { super(m); }
}