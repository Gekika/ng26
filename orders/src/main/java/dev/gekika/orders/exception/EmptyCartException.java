package dev.gekika.orders.exception;

public class EmptyCartException extends RuntimeException {
    public EmptyCartException() { super("Cannot checkout an empty cart"); }
}
