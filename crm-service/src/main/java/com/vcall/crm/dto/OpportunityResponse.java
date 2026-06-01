package com.vcall.crm.dto;

import com.vcall.crm.entity.OpportunityStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpportunityResponse {

    private UUID id;
    private UUID leadId;
    private String leadName;
    private String title;
    private String description;
    private BigDecimal value;
    private String currency;
    private OpportunityStage stage;
    private Integer probability;
    private LocalDate expectedCloseDate;
    private UUID assignedTo;
    private LocalDateTime createdAt;
}
