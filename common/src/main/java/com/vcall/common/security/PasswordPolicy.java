package com.vcall.common.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "security.password-policy")
public class PasswordPolicy {

    private int minLength = 8;
    private int maxLength = 100;
    private boolean requireUppercase = true;
    private boolean requireLowercase = true;
    private boolean requireDigit = true;
    private boolean requireSpecialChar = true;
    private int expiryDays = 90;
    private int historyCount = 5;
    private int maxAttempts = 5;
    private int lockoutDurationMinutes = 30;

    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]");

    public void validate(String password) {
        if (password == null || password.length() < minLength) {
            throw new IllegalArgumentException("Password must be at least " + minLength + " characters");
        }
        if (password.length() > maxLength) {
            throw new IllegalArgumentException("Password must not exceed " + maxLength + " characters");
        }
        if (requireUppercase && !UPPERCASE.matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }
        if (requireLowercase && !LOWERCASE.matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }
        if (requireDigit && !DIGIT.matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
        if (requireSpecialChar && !SPECIAL.matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one special character");
        }
    }
}
