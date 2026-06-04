package com.vcall.common.tenant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantProvisioningService {

    private final TrialLifecycleService trialLifecycleService;

    public TenantRegistrationResponse provisionTenant(TenantRegistrationRequest request) {
        String tenantId = "tenant-" + UUID.randomUUID().toString().substring(0, 8);

        log.info("Provisioning tenant: {} (ID: {})", request.getCompanyName(), tenantId);

        String plan = request.getPlan() != null ? request.getPlan() : "TRIAL";
        Subscription subscription = trialLifecycleService.activateTrial(tenantId, plan);

        TenantRegistrationResponse response = TenantRegistrationResponse.builder()
                .tenantId(tenantId)
                .companyName(request.getCompanyName())
                .adminUsername(request.getAdminEmail())
                .message("Tenant provisioned successfully. Welcome to VCall Contact Center!")
                .status("ACTIVE")
                .plan(plan)
                .trialEndDate(subscription.getTrialEnd())
                .maxAgents(subscription.getMaxAgents())
                .maxUsers(subscription.getMaxUsers())
                .build();

        log.info("Tenant {} provisioned successfully with admin {} (plan: {}, trial until {})",
                tenantId, request.getAdminEmail(), plan, subscription.getTrialEnd());
        return response;
    }

    public boolean isTenantAvailable(String companyName) {
        return true;
    }
}
