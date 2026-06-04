package com.vcall.apigateway.filter;

import com.vcall.apigateway.config.RateLimiterConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimiterFilter implements GlobalFilter, Ordered {

    private final RateLimiterConfig config;
    private final Map<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!config.isEnabled()) {
            return chain.filter(exchange);
        }

        String clientIp = getClientIp(exchange);
        String routeId = exchange.getAttribute(org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        if (routeId == null) {
            routeId = "default";
        }

        RateLimiterConfig.RouteLimit routeLimit = config.getRoutes().get(routeId);
        int capacity = routeLimit != null ? routeLimit.getCapacity() : config.getDefaultCapacity();
        int refillTokens = routeLimit != null ? routeLimit.getRefillTokens() : config.getDefaultRefillTokens();
        int refillDuration = routeLimit != null ? routeLimit.getRefillDuration() : config.getDefaultRefillDuration();

        String bucketKey = routeId + ":" + clientIp;
        RateLimitBucket bucket = buckets.computeIfAbsent(bucketKey,
                k -> new RateLimitBucket(capacity, refillTokens, refillDuration));

        if (bucket.tryConsume()) {
            return chain.filter(exchange);
        }

        log.warn("Rate limit exceeded for IP: {} on route: {}", clientIp, routeId);
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().add("Retry-After", String.valueOf(refillDuration));
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 100;
    }

    private String getClientIp(ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        return remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "unknown";
    }

    static class RateLimitBucket {
        private final int capacity;
        private final int refillTokens;
        private final long refillNanos;
        private long availableTokens;
        private Instant lastRefillTime;

        RateLimitBucket(int capacity, int refillTokens, int refillDurationSeconds) {
            this.capacity = capacity;
            this.refillTokens = refillTokens;
            this.refillNanos = Duration.ofSeconds(refillDurationSeconds).toNanos();
            this.availableTokens = capacity;
            this.lastRefillTime = Instant.now();
        }

        synchronized boolean tryConsume() {
            refill();
            if (availableTokens > 0) {
                availableTokens--;
                return true;
            }
            return false;
        }

        private void refill() {
            Instant now = Instant.now();
            long elapsed = Duration.between(lastRefillTime, now).toNanos();
            long tokensToAdd = (elapsed / refillNanos) * refillTokens;
            if (tokensToAdd > 0) {
                availableTokens = Math.min(capacity, availableTokens + tokensToAdd);
                lastRefillTime = lastRefillTime.plusNanos((elapsed / refillNanos) * refillNanos);
            }
        }
    }
}
