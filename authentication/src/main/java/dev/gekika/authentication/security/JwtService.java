package dev.gekika.authentication.security;

import dev.gekika.authentication.config.JwtProperties;
import dev.gekika.authentication.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey signingKey;
    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String ACCESS = "access";
    private static final String REFRESH = "refresh";

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        // Build the HMAC signing key from the configured secret.
        // This is the ONE place the secret is used — swapping to RSA
        // later means changing only this class.
        this.signingKey = Keys.hmacShaKeyFor(
                properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    /** Issue a signed access token carrying the user's identity and roles. */
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(properties.accessTokenTtl());

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList());

        return Jwts.builder()
                .issuer(properties.issuer())
                .subject(user.getId().toString())   // 'sub' = the user id
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Verify a token's signature and expiry, returning its claims.
     * Throws JwtException if the token is invalid, tampered, or expired —
     * that's how the filter (built next) will reject bad tokens.
     */
    public Claims parseAndValidate(String token) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(properties.issuer())
                .build()
                .parseSignedClaims(token);
        return jws.getPayload();
    }

    // A claim marking token purpose, so the two kinds can't be swapped.


    /** Issue a long-lived refresh token — a JWT carrying only the user id and a type marker. */
    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(properties.refreshTokenTtl());

        return Jwts.builder()
                .issuer(properties.issuer())
                .subject(user.getId().toString())
                .claim(TOKEN_TYPE_CLAIM, REFRESH)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Validate a refresh token: check signature, expiry, issuer, AND that it's
     * actually a refresh token. Returns the user id from the subject.
     */
    public UUID parseRefreshToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(properties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // Reject an access token presented at the refresh endpoint.
        if (!REFRESH.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new io.jsonwebtoken.JwtException("Not a refresh token");
        }
        return UUID.fromString(claims.getSubject());
    }
}
