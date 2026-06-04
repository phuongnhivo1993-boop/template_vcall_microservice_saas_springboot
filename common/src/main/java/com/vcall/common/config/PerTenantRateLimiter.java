package com.vcall.common.config;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class PerTenantRateLimiter {

    private final Map<String, SlidingWindowCounter> counters = new ConcurrentHashMap<>();
    private final int maxRequests;
    private final long windowMs;

    public PerTenantRateLimiter(int maxRequests, long windowMs) {
        this.maxRequests = maxRequests;
        this.windowMs = windowMs;
    }

    public boolean isAllowed(String tenantId) {
        SlidingWindowCounter counter = counters.computeIfAbsent(tenantId,
                k -> new SlidingWindowCounter(maxRequests, windowMs));
        return counter.tryAcquire();
    }

    public int getRemainingRequests(String tenantId) {
        SlidingWindowCounter counter = counters.get(tenantId);
        if (counter == null) return maxRequests;
        return maxRequests - counter.getCount();
    }

    private static class SlidingWindowCounter {
        private final int maxRequests;
        private final long windowMs;
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long windowStart;

        SlidingWindowCounter(int maxRequests, long windowMs) {
            this.maxRequests = maxRequests;
            this.windowMs = windowMs;
            this.windowStart = System.currentTimeMillis();
        }

        synchronized boolean tryAcquire() {
            long now = System.currentTimeMillis();
            if (now - windowStart >= windowMs) {
                windowStart = now;
                count.set(0);
            }
            if (count.get() >= maxRequests) {
                return false;
            }
            count.incrementAndGet();
            return true;
        }

        int getCount() {
            long now = System.currentTimeMillis();
            if (now - windowStart >= windowMs) {
                return 0;
            }
            return count.get();
        }
    }
}
