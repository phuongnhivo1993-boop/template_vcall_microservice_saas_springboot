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
public class AuditSearchRequest {
    private UUID actorId;
    private String action;
    private String resource;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;
}
