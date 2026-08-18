package dev.gekika.authentication.security;

import dev.gekika.authentication.config.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // Only try to authenticate if a Bearer token is present.
        // No token -> continue as an anonymous request; endpoint-level
        // checks decide whether that's allowed.
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwtService.parseAndValidate(token);

                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);

                AuthenticatedUser user = new AuthenticatedUser(
                        UUID.fromString(claims.getSubject()),
                        claims.get("email", String.class),
                        roles
                );

                // Stash the authenticated caller on the request so
                // controllers (and the resolver) can read it.
                request.setAttribute(SecurityConstants.AUTH_USER_ATTRIBUTE, user);

            } catch (JwtException | IllegalArgumentException ex) {
                // Token present but invalid/expired/tampered.
                // Reject outright — a bad token is never "anonymous".
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                        "Invalid or expired token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}