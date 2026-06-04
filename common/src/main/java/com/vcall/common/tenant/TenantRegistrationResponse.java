package com.vcall.common.tenant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantRegistrationResponse {
    private String tenantId;
    private String companyName;
    private String adminUsername;
    private String message;
    private String status;
    private String plan;
    private LocalDateTime trialEndDate;
    private Integer maxAgents;
    private Integer maxUsers;
}
