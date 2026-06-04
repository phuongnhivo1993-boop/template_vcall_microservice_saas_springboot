package com.vcall.common.tenant;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Subscription {
    private String id;
    private String tenantId;
    private String plan;
    private String status;
    private LocalDateTime trialStart;
    private LocalDateTime trialEnd;
    private LocalDateTime currentPeriodStart;
    private LocalDateTime currentPeriodEnd;
    private int maxUsers;
    private int maxAgents;
    private int maxCallsPerMonth;
    private boolean autoRenew;
}
