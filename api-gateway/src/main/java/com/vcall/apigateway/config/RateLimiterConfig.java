package com.vcall.apigateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterConfig {

    private boolean enabled = true;
    private int defaultCapacity = 100;
    private int defaultRefillTokens = 10;
    private int defaultRefillDuration = 1;
    private Map<String, RouteLimit> routes = new HashMap<>();

    @Data
    public static class RouteLimit {
        private int capacity;
        private int refillTokens;
        private int refillDuration;
    }
}
