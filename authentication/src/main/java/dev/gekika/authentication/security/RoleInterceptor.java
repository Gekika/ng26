package dev.gekika.authentication.security;

import dev.gekika.authentication.config.SecurityConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
@RequiredArgsConstructor

public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // Only method handlers carry annotations; let others pass.
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // Look for @RequireRole on the method first, then the class.
        RequireRole annotation = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (annotation == null) {
            annotation = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }

        // No annotation -> no role requirement -> allow.
        if (annotation == null) {
            return true;
        }

        // The route requires a role, so it requires authentication.
        AuthenticatedUser user = (AuthenticatedUser) request.getAttribute(
                SecurityConstants.AUTH_USER_ATTRIBUTE);

        if (user == null) {
            // No valid token was present -> 401, not 403.
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "Authentication required");
            return false;
        }

        // Caller needs at least one of the required roles.
        boolean allowed = Arrays.stream(annotation.value())
                .anyMatch(user::hasRole);

        if (!allowed) {
            // Authenticated, but insufficient role -> 403.
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Insufficient permissions");
            return false;
        }

        return true;  // cleared — proceed to the controller
    }
}
