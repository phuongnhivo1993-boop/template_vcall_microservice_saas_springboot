package com.vcall.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlaReport {

    private String period;
    private Long totalTickets;
    private Long breached;
    private Long compliant;
    private Double slaComplianceRate;
}
