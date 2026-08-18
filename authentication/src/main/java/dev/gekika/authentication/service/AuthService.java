package dev.gekika.authentication.service;

import dev.gekika.authentication.config.JwtProperties;
import dev.gekika.authentication.dto.LoginRequest;
import dev.gekika.authentication.dto.RegisterRequest;
import dev.gekika.authentication.dto.TokenResponse;
import dev.gekika.authentication.dto.UserResponse;
import dev.gekika.authentication.exception.EmailAlreadyExistsException;
import dev.gekika.authentication.exception.InvalidCredentialsException;
import dev.gekika.authentication.exception.InvalidTokenException;
import dev.gekika.authentication.exception.RoleNotFoundException;
import dev.gekika.authentication.model.Role;
import dev.gekika.authentication.model.User;
import dev.gekika.authentication.repo.RoleRepository;
import dev.gekika.authentication.repo.UserRepository;
import dev.gekika.authentication.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;


    @Transactional
    public UserResponse register(RegisterRequest request) {
        // 1. Reject duplicates up front with a clear, specific error.
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        // 2. Every self-registration is a CUSTOMER. Fetch that role from
        //    the DB (it was seeded). Elevating to SELLER/ADMIN is a
        //    separate, protected operation later.
        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new RoleNotFoundException("CUSTOMER"));

        // 3. Hash the password. The raw value dies with this method —
        //    only the hash is ever persisted.
        String passwordHash = passwordEncoder.encode(request.password());

        // 4. Build and save the user.
        User user = new User(request.email(), passwordHash, Set.of(customerRole));
        User saved = userRepository.save(user);

        // 5. Map entity -> response DTO. Roles become their names.
        return toResponse(saved);
    }

    private UserResponse toResponse(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        return new UserResponse(user.getId(), user.getEmail(), roleNames);
    }

    public TokenResponse login(LoginRequest request) {
        // Look up the user. If not found, we STILL fall through to a generic
        // "invalid credentials" error — never reveal whether the email exists.
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        // Verify the password against the stored BCrypt hash.
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        // Reject disabled accounts.
        if (!user.isEnabled()) {
            throw new InvalidCredentialsException();
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new TokenResponse(accessToken, refreshToken, "Bearer",
                jwtProperties.accessTokenTtl().toSeconds());
    }

    public TokenResponse refresh(String refreshToken) {
        UUID userId;
        try {
            userId = jwtService.parseRefreshToken(refreshToken);
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException ex) {
            throw new InvalidTokenException();
        }

        // Reload the user so the new access token reflects CURRENT roles,
        // and so a deleted/disabled account can't keep refreshing.
        User user = userRepository.findById(userId)
                .orElseThrow(InvalidTokenException::new);
        if (!user.isEnabled()) {
            throw new InvalidTokenException();
        }

        String newAccess = jwtService.generateAccessToken(user);
        // Optionally re-issue the refresh token too, or keep the old one until it expires.
        String newRefresh = jwtService.generateRefreshToken(user);

        return new TokenResponse(newAccess, newRefresh, "Bearer",
                jwtProperties.accessTokenTtl().toSeconds());
    }
}