package dev.gekika.orders.exception;


public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String m) { super(m); }
}