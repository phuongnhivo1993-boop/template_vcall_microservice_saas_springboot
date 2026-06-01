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
public class AuditLogResponse {
    private UUID id;
    private LocalDateTime timestamp;
    private UUID actorId;
    private String actorType;
    private String action;
    private String resource;
    private String resourceId;
    private String resourceType;
    private String details;
    private String ipAddress;
    private String correlationId;
    private String status;
}
