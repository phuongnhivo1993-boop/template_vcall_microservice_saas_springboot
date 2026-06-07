package com.vcall.iam.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.iam.entity.User;
import com.vcall.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    private static final long TOKEN_VALIDITY_MINUTES = 30;
    private static final String TOKEN_KEY_PREFIX = "password-reset:";

    public String generateResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with email: " + email));

        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        redisTemplate.opsForValue().set(
                TOKEN_KEY_PREFIX + token,
                user.getId().toString(),
                Duration.ofMinutes(TOKEN_VALIDITY_MINUTES)
        );
        log.info("Password reset token generated for user: {}", user.getUsername());
        return token;
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        String userIdStr = redisTemplate.opsForValue().get(TOKEN_KEY_PREFIX + token);
        if (userIdStr == null) {
            throw new IllegalArgumentException("Invalid or expired reset token");
        }

        java.util.UUID userId;
        try {
            userId = java.util.UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid or expired reset token");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        redisTemplate.delete(TOKEN_KEY_PREFIX + token);
        log.info("Password reset successful for user: {}", user.getUsername());
    }
}
