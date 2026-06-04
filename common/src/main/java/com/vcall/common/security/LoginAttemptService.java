package com.vcall.common.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class LoginAttemptService {

    private final Map<String, AttemptData> attempts = new ConcurrentHashMap<>();

    public void recordFailedAttempt(String username) {
        AttemptData data = attempts.computeIfAbsent(username.toLowerCase(), k -> new AttemptData());
        data.count++;
        data.lastAttempt = Instant.now();
        log.warn("Failed login attempt {} for user: {}", data.count, username);
    }

    public boolean isLocked(String username) {
        AttemptData data = attempts.get(username.toLowerCase());
        if (data == null) return false;

        int maxAttempts = 5;
        if (data.count >= maxAttempts) {
            if (data.lockoutStart != null) {
                long elapsed = Instant.now().toEpochMilli() - data.lockoutStart.toEpochMilli();
                if (elapsed < 30 * 60 * 1000) {
                    return true;
                }
                data.count = 0;
                data.lockoutStart = null;
                return false;
            }
            data.lockoutStart = Instant.now();
            return true;
        }
        return false;
    }

    public void resetAttempts(String username) {
        attempts.remove(username.toLowerCase());
    }

    public int getRemainingAttempts(String username) {
        AttemptData data = attempts.get(username.toLowerCase());
        if (data == null) return 5;
        return Math.max(0, 5 - data.count);
    }

    private static class AttemptData {
        int count;
        Instant lastAttempt;
        Instant lockoutStart;
    }
}
