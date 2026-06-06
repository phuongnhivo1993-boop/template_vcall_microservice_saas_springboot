package com.vcall.iam.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.iam.entity.User;
import com.vcall.iam.kafka.UserEventPublisher;
import com.vcall.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final UserEventPublisher eventPublisher;

    private static final int TOKEN_EXPIRY_HOURS = 24;

    @Transactional
    public void sendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (user.isEmailVerified()) {
            throw new IllegalStateException("Email is already verified");
        }

        String token = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        user.setEmailVerificationToken(token);
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS));
        userRepository.save(user);

        eventPublisher.publishUserUpdated(user);
        log.info("Verification email sent to: {}", email);
    }

    @Transactional
    public void sendVerificationEmailForUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.isEmailVerified()) {
            throw new IllegalStateException("Email is already verified");
        }

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalStateException("User does not have an email address");
        }

        sendVerificationEmail(user.getEmail());
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        if (user.isEmailVerified()) {
            return;
        }

        if (user.getEmailVerificationTokenExpiry() == null ||
                user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Verification token has expired. Please request a new one.");
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        userRepository.save(user);

        eventPublisher.publishUserUpdated(user);
        log.info("Email verified for user: {}", user.getUsername());
    }
}
