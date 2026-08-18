package dev.gekika.authentication.exception;


public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(String roleName) {
        super("Role '" + roleName + "' is not configured in the system");
    }
}