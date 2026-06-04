package com.vcall.common.tenant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureLimitEnforcer {

    private final TrialLifecycleService trialLifecycleService;

    public boolean canCreateAgent(String tenantId) {
        var sub = trialLifecycleService.getSubscription(tenantId);
        if (sub == null) return true;
        return sub.getMaxAgents() > 0;
    }

    public boolean canCreateUser(String tenantId, int currentUserCount) {
        var sub = trialLifecycleService.getSubscription(tenantId);
        if (sub == null) return true;
        return currentUserCount < sub.getMaxUsers();
    }

    public boolean canProcessCall(String tenantId, int currentMonthlyCalls) {
        var sub = trialLifecycleService.getSubscription(tenantId);
        if (sub == null) return true;
        return currentMonthlyCalls < sub.getMaxCallsPerMonth();
    }

    public int getMaxAgents(String tenantId) {
        var sub = trialLifecycleService.getSubscription(tenantId);
        if (sub == null) return Integer.MAX_VALUE;
        return sub.getMaxAgents();
    }

    public int getMaxUsers(String tenantId) {
        var sub = trialLifecycleService.getSubscription(tenantId);
        if (sub == null) return Integer.MAX_VALUE;
        return sub.getMaxUsers();
    }

    public int getMaxMonthlyCalls(String tenantId) {
        var sub = trialLifecycleService.getSubscription(tenantId);
        if (sub == null) return Integer.MAX_VALUE;
        return sub.getMaxCallsPerMonth();
    }
}
