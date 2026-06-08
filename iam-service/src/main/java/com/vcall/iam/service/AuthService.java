package com.vcall.iam.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.common.security.LoginAttemptService;
import com.vcall.iam.dto.LoginRequest;
import com.vcall.iam.dto.LoginResponse;
import com.vcall.iam.dto.RefreshTokenRequest;
import com.vcall.iam.dto.UserResponse;
import com.vcall.iam.entity.RefreshToken;
import com.vcall.iam.entity.User;
import com.vcall.iam.entity.UserStatus;
import com.vcall.iam.repository.RefreshTokenRepository;
import com.vcall.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final UserService userService;
    private final LoginAttemptService loginAttemptService;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername().toLowerCase();

        if (loginAttemptService.isLocked(username)) {
            throw new IllegalStateException("Account is temporarily locked due to too many failed attempts. Try again in 30 minutes.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword())
            );
            loginAttemptService.resetAttempts(username);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new IllegalStateException("User account is not active");
            }

            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            if (user.isMfaEnabled()) {
                String mfaToken = jwtService.generateMfaToken(user.getUsername());
                return LoginResponse.builder()
                        .mfaRequired(true)
                        .mfaToken(mfaToken)
                        .tokenType("Bearer")
                        .user(userService.toUserResponse(user))
                        .build();
            }

            Map<String, Object> claims = new HashMap<>();
            Set<String> roles = user.getUserRoles().stream()
                    .map(ur -> ur.getRole().getName().name())
                    .collect(Collectors.toSet());
            claims.put("roles", roles);
            claims.put("userId", user.getId().toString());
            claims.put("tenantId", user.getTenantId() != null ? user.getTenantId() : "default");

            String accessToken = jwtService.generateAccessToken(user.getUsername(), claims);
            String refreshTokenValue = jwtService.generateRefreshToken(user.getUsername());
            String family = UUID.randomUUID().toString();

            RefreshToken refreshToken = RefreshToken.builder()
                    .token(refreshTokenValue)
                    .userId(user.getId())
                    .family(family)
                    .expiresAt(Instant.now().plusMillis(refreshExpiration))
                    .revoked(false)
                    .build();
            refreshTokenRepository.save(refreshToken);

            UserResponse userResponse = userService.toUserResponse(user);

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshTokenValue)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getExpiration())
                    .user(userResponse)
                    .build();
        } catch (BadCredentialsException e) {
            loginAttemptService.recordFailedAttempt(username);
            int remaining = loginAttemptService.getRemainingAttempts(username);
            throw new BadCredentialsException("Invalid credentials. " + remaining + " attempts remaining.");
        }
    }

    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String tokenValue = request.getRefreshToken();
        RefreshToken storedToken = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (storedToken.isRevoked()) {
            refreshTokenRepository.findByFamily(storedToken.getFamily())
                    .forEach(t -> {
                        t.setRevoked(true);
                        refreshTokenRepository.save(t);
                    });
            log.warn("Refresh token reuse detected for family: {}. All tokens in family revoked.",
                    storedToken.getFamily());
            throw new IllegalArgumentException("Refresh token has been revoked");
        }

        if (Instant.now().isAfter(storedToken.getExpiresAt())) {
            refreshTokenRepository.delete(storedToken);
            throw new IllegalArgumentException("Refresh token has expired");
        }

        if (!jwtService.validateToken(tokenValue)) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String username = jwtService.extractUsername(tokenValue);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        Map<String, Object> claims = new HashMap<>();
        Set<String> roles = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName().name())
                .collect(Collectors.toSet());
        claims.put("roles", roles);
        claims.put("userId", user.getId().toString());

        String newAccessToken = jwtService.generateAccessToken(username, claims);
        String newRefreshTokenValue = jwtService.generateRefreshToken(username);

        RefreshToken newRefreshToken = RefreshToken.builder()
                .token(newRefreshTokenValue)
                .userId(user.getId())
                .family(storedToken.getFamily())
                .expiresAt(Instant.now().plusMillis(refreshExpiration))
                .revoked(false)
                .build();
        refreshTokenRepository.save(newRefreshToken);

        UserResponse userResponse = userService.toUserResponse(user);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiration())
                .user(userResponse)
                .build();
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    @Transactional
    public void logoutAll(UUID userId) {
        refreshTokenRepository.findByUserId(userId).forEach(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return userService.toUserResponse(user);
    }
}
