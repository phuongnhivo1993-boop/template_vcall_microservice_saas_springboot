package com.vcall.automation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionLogResponse {

    private Long id;
    private Long ruleId;
    private String triggeredBy;
    private String status;
    private String errorMessage;
    private LocalDateTime executedAt;
}
