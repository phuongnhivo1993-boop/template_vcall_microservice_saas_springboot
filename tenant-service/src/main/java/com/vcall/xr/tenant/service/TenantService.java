package com.vcall.xr.tenant.service;

import com.vcall.common.exception.DuplicateResourceException;
import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.xr.tenant.domain.FeatureFlag;
import com.vcall.xr.tenant.domain.Subscription;
import com.vcall.xr.tenant.domain.Tenant;
import com.vcall.xr.tenant.domain.TenantPlan;
import com.vcall.xr.tenant.domain.TenantStatus;
import com.vcall.xr.tenant.dto.FeatureFlagRequest;
import com.vcall.xr.tenant.dto.FeatureFlagResponse;
import com.vcall.xr.tenant.dto.PlanUpgradeRequest;
import com.vcall.xr.tenant.dto.TenantRequest;
import com.vcall.xr.tenant.dto.TenantResponse;
import com.vcall.xr.tenant.mapper.FeatureFlagMapper;
import com.vcall.xr.tenant.mapper.TenantMapper;
import com.vcall.xr.tenant.repository.FeatureFlagRepository;
import com.vcall.xr.tenant.repository.SubscriptionRepository;
import com.vcall.xr.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final FeatureFlagRepository featureFlagRepository;
    private final TenantMapper tenantMapper;
    private final FeatureFlagMapper featureFlagMapper;

    @Transactional
    @CacheEvict(value = "tenants", allEntries = true)
    public TenantResponse createTenant(TenantRequest request) {
        if (tenantRepository.existsBySlug(request.getSlug())) {
            throw new DuplicateResourceException("Tenant slug already exists: " + request.getSlug());
        }
        if (tenantRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Tenant name already exists: " + request.getName());
        }

        Tenant tenant = tenantMapper.toEntity(request);
        tenant.setStatus(TenantStatus.ACTIVE);

        if (tenant.getMaxScenes() == null) {
            tenant.setMaxScenes(getDefaultMaxScenes(request.getPlan()));
        }
        if (tenant.getMaxStorageGb() == null) {
            tenant.setMaxStorageGb(getDefaultMaxStorageGb(request.getPlan()));
        }
        if (tenant.getMaxBandwidthGb() == null) {
            tenant.setMaxBandwidthGb(getDefaultMaxBandwidthGb(request.getPlan()));
        }

        tenant = tenantRepository.save(tenant);

        Subscription subscription = Subscription.builder()
                .tenant(tenant)
                .plan(request.getPlan())
                .status(com.vcall.xr.tenant.domain.SubscriptionStatus.ACTIVE)
                .billingCycle("MONTHLY")
                .priceCents(getPriceCents(request.getPlan()))
                .storageUsedBytes(0L)
                .bandwidthUsedBytes(0L)
                .scenesCount(0)
                .usersCount(0)
                .currentPeriodStart(LocalDateTime.now())
                .currentPeriodEnd(LocalDateTime.now().plusMonths(1))
                .build();
        subscriptionRepository.save(subscription);

        return tenantMapper.toResponse(tenant);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "tenants", key = "#id")
    public TenantResponse getTenantById(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));
        return tenantMapper.toResponse(tenant);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "tenants", key = "'slug_' + #slug")
    public TenantResponse getTenantBySlug(String slug) {
        Tenant tenant = tenantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with slug: " + slug));
        return tenantMapper.toResponse(tenant);
    }

    @Transactional(readOnly = true)
    public Page<TenantResponse> getAllTenants(Pageable pageable) {
        return tenantRepository.findAll(pageable).map(tenantMapper::toResponse);
    }

    @Transactional
    @CacheEvict(value = "tenants", allEntries = true)
    public TenantResponse updateTenant(UUID id, TenantRequest request) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));

        if (request.getSlug() != null && !request.getSlug().equals(tenant.getSlug())
                && tenantRepository.existsBySlug(request.getSlug())) {
            throw new DuplicateResourceException("Tenant slug already exists: " + request.getSlug());
        }

        tenantMapper.updateEntity(request, tenant);
        tenant = tenantRepository.save(tenant);
        return tenantMapper.toResponse(tenant);
    }

    @Transactional
    @CacheEvict(value = "tenants", allEntries = true)
    public void deleteTenant(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));
        tenant.setIsDeleted(true);
        tenantRepository.save(tenant);
    }

    @Transactional
    public TenantResponse suspendTenant(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));
        tenant.setStatus(TenantStatus.SUSPENDED);
        tenant = tenantRepository.save(tenant);
        return tenantMapper.toResponse(tenant);
    }

    @Transactional
    public TenantResponse activateTenant(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant = tenantRepository.save(tenant);
        return tenantMapper.toResponse(tenant);
    }

    @Transactional
    public TenantResponse upgradePlan(UUID id, PlanUpgradeRequest request) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));

        tenant.setPlan(request.getPlan());
        tenant.setMaxScenes(getDefaultMaxScenes(request.getPlan()));
        tenant.setMaxStorageGb(getDefaultMaxStorageGb(request.getPlan()));
        tenant.setMaxBandwidthGb(getDefaultMaxBandwidthGb(request.getPlan()));

        tenant = tenantRepository.save(tenant);

        Subscription subscription = Subscription.builder()
                .tenant(tenant)
                .plan(request.getPlan())
                .status(com.vcall.xr.tenant.domain.SubscriptionStatus.ACTIVE)
                .billingCycle(request.getBillingCycle() != null ? request.getBillingCycle() : "MONTHLY")
                .priceCents(getPriceCents(request.getPlan()))
                .storageUsedBytes(0L)
                .bandwidthUsedBytes(0L)
                .scenesCount(0)
                .usersCount(0)
                .currentPeriodStart(LocalDateTime.now())
                .currentPeriodEnd(LocalDateTime.now().plusMonths(1))
                .build();
        subscriptionRepository.save(subscription);

        return tenantMapper.toResponse(tenant);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "tenants", key = "'features_' + #tenantId")
    public List<FeatureFlagResponse> getFeatureFlags(UUID tenantId) {
        return featureFlagRepository.findByTenantId(tenantId).stream()
                .map(featureFlagMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "tenants", key = "'feature_' + #tenantId + '_' + #featureKey")
    public FeatureFlagResponse getFeatureFlag(UUID tenantId, String featureKey) {
        FeatureFlag flag = featureFlagRepository.findByTenantIdAndFeatureKey(tenantId, featureKey)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Feature flag not found for tenant " + tenantId + " with key: " + featureKey));
        return featureFlagMapper.toResponse(flag);
    }

    @Transactional
    @CacheEvict(value = "tenants", allEntries = true)
    public FeatureFlagResponse createFeatureFlag(UUID tenantId, FeatureFlagRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId));

        if (featureFlagRepository.existsByTenantIdAndFeatureKey(tenantId, request.getFeatureKey())) {
            throw new DuplicateResourceException(
                    "Feature flag already exists for tenant " + tenantId + " with key: " + request.getFeatureKey());
        }

        FeatureFlag flag = featureFlagMapper.toEntity(request);
        flag.setTenant(tenant);
        flag = featureFlagRepository.save(flag);
        return featureFlagMapper.toResponse(flag);
    }

    @Transactional
    @CacheEvict(value = "tenants", allEntries = true)
    public FeatureFlagResponse updateFeatureFlag(UUID tenantId, UUID flagId, FeatureFlagRequest request) {
        FeatureFlag flag = featureFlagRepository.findById(flagId)
                .orElseThrow(() -> new ResourceNotFoundException("Feature flag not found with id: " + flagId));

        featureFlagMapper.updateEntity(request, flag);
        flag = featureFlagRepository.save(flag);
        return featureFlagMapper.toResponse(flag);
    }

    @Transactional
    @CacheEvict(value = "tenants", allEntries = true)
    public void deleteFeatureFlag(UUID tenantId, UUID flagId) {
        FeatureFlag flag = featureFlagRepository.findById(flagId)
                .orElseThrow(() -> new ResourceNotFoundException("Feature flag not found with id: " + flagId));
        flag.setIsDeleted(true);
        featureFlagRepository.save(flag);
    }

    @Transactional
    public boolean isFeatureEnabled(UUID tenantId, String featureKey) {
        return featureFlagRepository.findByTenantIdAndFeatureKey(tenantId, featureKey)
                .map(FeatureFlag::isEnabled)
                .orElse(false);
    }

    private int getDefaultMaxScenes(TenantPlan plan) {
        return switch (plan) {
            case FREE -> 5;
            case STARTER -> 25;
            case PRO -> 100;
            case ENTERPRISE -> Integer.MAX_VALUE;
        };
    }

    private long getDefaultMaxStorageGb(TenantPlan plan) {
        return switch (plan) {
            case FREE -> 10L;
            case STARTER -> 100L;
            case PRO -> 500L;
            case ENTERPRISE -> 10000L;
        };
    }

    private long getDefaultMaxBandwidthGb(TenantPlan plan) {
        return switch (plan) {
            case FREE -> 50L;
            case STARTER -> 500L;
            case PRO -> 2000L;
            case ENTERPRISE -> 50000L;
        };
    }

    private long getPriceCents(TenantPlan plan) {
        return switch (plan) {
            case FREE -> 0L;
            case STARTER -> 2999L;
            case PRO -> 9999L;
            case ENTERPRISE -> 49999L;
        };
    }
}
