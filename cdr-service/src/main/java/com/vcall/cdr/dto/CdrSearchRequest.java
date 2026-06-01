package com.vcall.cdr.dto;

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
public class CdrSearchRequest {

    private String callerNumber;
    private String calleeNumber;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private UUID agentId;
    private String direction;
    private int page;
    private int size;
}
