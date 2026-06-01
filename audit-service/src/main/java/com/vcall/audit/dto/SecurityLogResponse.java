package com.vcall.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityLogResponse {
    private UUID id;
    private LocalDateTime timestamp;
    private String eventType;
    private UUID actorId;
    private String username;
    private String ipAddress;
    private String userAgent;
    private String details;
    private String riskLevel;
    private Boolean isSuspicious;
}
