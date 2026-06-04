package com.vcall.common.tenant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class TrialLifecycleService {

    private static final int TRIAL_DAYS = 14;
    private static final int EXPIRY_WARNING_DAYS = 3;

    private final Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();

    public Subscription activateTrial(String tenantId, String plan) {
        Subscription sub = new Subscription();
        sub.setId("sub_" + tenantId);
        sub.setTenantId(tenantId);
        sub.setPlan(plan != null ? plan : "TRIAL");
        sub.setStatus("TRIAL");
        sub.setTrialStart(LocalDateTime.now());
        sub.setTrialEnd(LocalDateTime.now().plusDays(TRIAL_DAYS));
        sub.setCurrentPeriodStart(LocalDateTime.now());
        sub.setCurrentPeriodEnd(LocalDateTime.now().plusDays(TRIAL_DAYS));
        sub.setMaxUsers(10);
        sub.setMaxAgents(5);
        sub.setMaxCallsPerMonth(500);
        sub.setAutoRenew(false);
        subscriptions.put(tenantId, sub);
        log.info("Trial activated for tenant {} until {}", tenantId, sub.getTrialEnd());
        return sub;
    }

    public boolean isTrialExpired(String tenantId) {
        Subscription sub = subscriptions.get(tenantId);
        if (sub == null) return false;
        if (!"TRIAL".equals(sub.getStatus())) return false;
        return LocalDateTime.now().isAfter(sub.getTrialEnd());
    }

    public int getDaysRemaining(String tenantId) {
        Subscription sub = subscriptions.get(tenantId);
        if (sub == null) return 0;
        return (int) ChronoUnit.DAYS.between(LocalDateTime.now(), sub.getTrialEnd());
    }

    public boolean shouldWarnExpiry(String tenantId) {
        int days = getDaysRemaining(tenantId);
        return days > 0 && days <= EXPIRY_WARNING_DAYS;
    }

    public Subscription convertToPaid(String tenantId, String plan) {
        Subscription sub = subscriptions.get(tenantId);
        if (sub == null) {
            log.warn("No subscription found for tenant {}", tenantId);
            return null;
        }
        sub.setPlan(plan);
        sub.setStatus("ACTIVE");
        sub.setCurrentPeriodStart(LocalDateTime.now());
        sub.setCurrentPeriodEnd(LocalDateTime.now().plusMonths(1));
        sub.setAutoRenew(true);
        switch (plan) {
            case "BASIC":
                sub.setMaxUsers(25);
                sub.setMaxAgents(10);
                sub.setMaxCallsPerMonth(2000);
                break;
            case "PRO":
                sub.setMaxUsers(100);
                sub.setMaxAgents(50);
                sub.setMaxCallsPerMonth(10000);
                break;
            case "ENTERPRISE":
                sub.setMaxUsers(1000);
                sub.setMaxAgents(500);
                sub.setMaxCallsPerMonth(100000);
                break;
            default:
                break;
        }
        log.info("Tenant {} converted to {} plan", tenantId, plan);
        return sub;
    }

    public Subscription getSubscription(String tenantId) {
        return subscriptions.get(tenantId);
    }
}
