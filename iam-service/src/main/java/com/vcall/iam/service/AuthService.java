package com.vcall.iam.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.iam.dto.LoginRequest;
import com.vcall.iam.dto.LoginResponse;
import com.vcall.iam.dto.RefreshTokenRequest;
import com.vcall.iam.dto.UserResponse;
import com.vcall.iam.entity.User;
import com.vcall.iam.entity.UserStatus;
import com.vcall.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserService userService;

    private final Map<String, String> refreshTokens = new ConcurrentHashMap<>();

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("User account is not active");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        Map<String, Object> claims = new HashMap<>();
        Set<String> roles = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName().name())
                .collect(Collectors.toSet());
        claims.put("roles", roles);
        claims.put("userId", user.getId().toString());

        String accessToken = jwtService.generateAccessToken(user.getUsername(), claims);
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());
        refreshTokens.put(refreshToken, user.getUsername());

        UserResponse userResponse = userService.toUserResponse(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiration())
                .user(userResponse)
                .build();
    }

    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        String username = refreshTokens.get(token);

        if (username == null || !jwtService.validateToken(token)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        refreshTokens.remove(token);

        Map<String, Object> claims = new HashMap<>();
        Set<String> roles = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName().name())
                .collect(Collectors.toSet());
        claims.put("roles", roles);
        claims.put("userId", user.getId().toString());

        String newAccessToken = jwtService.generateAccessToken(username, claims);
        String newRefreshToken = jwtService.generateRefreshToken(username);
        refreshTokens.put(newRefreshToken, username);

        UserResponse userResponse = userService.toUserResponse(user);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiration())
                .user(userResponse)
                .build();
    }

    public void logout(String refreshToken) {
        refreshTokens.remove(refreshToken);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return userService.toUserResponse(user);
    }
}
