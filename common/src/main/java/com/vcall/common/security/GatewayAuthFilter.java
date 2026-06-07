package com.vcall.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GatewayAuthFilter extends OncePerRequestFilter {

    private static final String GATEWAY_SECRET_HEADER = "X-Gateway-Secret";

    @Value("${gateway.secret:}")
    private String expectedGatewaySecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String userId = request.getHeader("X-User-Id");
        String userRoles = request.getHeader("X-User-Roles");
        String gatewaySecret = request.getHeader(GATEWAY_SECRET_HEADER);

        if (userId != null && !userId.isEmpty()) {
            if (expectedGatewaySecret != null && !expectedGatewaySecret.isEmpty()) {
                if (gatewaySecret == null || !gatewaySecret.equals(expectedGatewaySecret)) {
                    log.warn("Request to {} from {} has X-User-Id header but missing/invalid gateway secret – rejecting authentication",
                            request.getRequestURI(), request.getRemoteAddr());
                    filterChain.doFilter(request, response);
                    return;
                }
            } else {
                log.warn("Gateway secret not configured. Request to {} from {} has X-User-Id header without gateway secret verification",
                        request.getRequestURI(), request.getRemoteAddr());
            }

            List<SimpleGrantedAuthority> authorities = List.of();
            if (userRoles != null && !userRoles.isEmpty()) {
                String rolesStr = userRoles.replace("[", "").replace("]", "");
                authorities = Arrays.stream(rolesStr.split(","))
                        .map(String::trim)
                        .filter(r -> !r.isEmpty())
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());
            }

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}
