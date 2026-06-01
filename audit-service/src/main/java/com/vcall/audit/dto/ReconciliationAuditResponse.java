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
public class ReconciliationAuditResponse {
    private UUID id;
    private LocalDateTime reconciliationDate;
    private String type;
    private Integer totalRecords;
    private Integer matchedCount;
    private Integer unmatchedCount;
    private Integer discrepancyCount;
    private String status;
}
