package com.vcall.crm.dto;

import com.vcall.crm.entity.OpportunityStage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpportunityRequest {

    @NotNull
    private UUID leadId;

    @NotBlank
    private String title;

    private String description;

    private BigDecimal value;

    private String currency;

    private OpportunityStage stage;

    private Integer probability;

    private LocalDate expectedCloseDate;

    private UUID assignedTo;
}
