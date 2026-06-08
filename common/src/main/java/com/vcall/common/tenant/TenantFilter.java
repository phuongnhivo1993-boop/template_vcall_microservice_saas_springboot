package com.vcall.common.tenant;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
@Order(1)
public class TenantFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String tenantId = extractTenantId(request);
            if (tenantId != null) {
                TenantContext.setTenantId(tenantId);
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String extractTenantId(HttpServletRequest request) {
        String tenantId = request.getHeader("X-Tenant-Id");
        if (tenantId != null && !tenantId.isEmpty()) {
            return tenantId;
        }

        tenantId = request.getParameter("tenantId");
        if (tenantId != null && !tenantId.isEmpty()) {
            return tenantId;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tenantId = extractTenantFromJwt(token);
            if (tenantId != null) {
                return tenantId;
            }
        }

        return null;
    }

    private String extractTenantFromJwt(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length >= 2) {
                String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                Map<String, Object> claims = objectMapper.readValue(payload,
                        new TypeReference<Map<String, Object>>() {});
                Object tenantClaim = claims.get("tenantId");
                Object expClaim = claims.get("exp");
                if (tenantClaim != null) {
                    if (expClaim instanceof Number) {
                        long exp = ((Number) expClaim).longValue() * 1000;
                        if (System.currentTimeMillis() > exp) {
                            log.warn("Expired JWT token during tenant extraction");
                            return null;
                        }
                    }
                    return tenantClaim.toString();
                }
            }
        } catch (Exception e) {
            log.trace("Could not extract tenant from JWT: {}", e.getMessage());
        }
        return null;
    }
}
