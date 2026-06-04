package com.vcall.iam.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.iam.entity.User;
import com.vcall.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final Map<String, ResetTokenData> resetTokens = new ConcurrentHashMap<>();

    private static final long TOKEN_VALIDITY_MINUTES = 30;

    public String generateResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with email: " + email));

        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        resetTokens.put(token, new ResetTokenData(user.getId(), Instant.now()));
        log.info("Password reset token generated for user: {}", user.getUsername());
        return token;
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        ResetTokenData data = resetTokens.get(token);
        if (data == null) {
            throw new IllegalArgumentException("Invalid or expired reset token");
        }

        if (Instant.now().isAfter(data.createdAt().plusSeconds(TOKEN_VALIDITY_MINUTES * 60))) {
            resetTokens.remove(token);
            throw new IllegalArgumentException("Reset token has expired");
        }

        User user = userRepository.findById(data.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetTokens.remove(token);
        log.info("Password reset successful for user: {}", user.getUsername());
    }

    private record ResetTokenData(java.util.UUID userId, Instant createdAt) {}
}
