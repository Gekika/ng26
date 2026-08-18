package dev.gekika.authentication.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Usable on a class (guards every method) or a single method.
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    // The roles allowed. Caller needs AT LEAST ONE of them.
    String[] value();
}