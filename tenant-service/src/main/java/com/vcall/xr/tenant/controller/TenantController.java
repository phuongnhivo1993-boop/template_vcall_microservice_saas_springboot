package com.vcall.xr.tenant.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.xr.tenant.dto.FeatureFlagRequest;
import com.vcall.xr.tenant.dto.FeatureFlagResponse;
import com.vcall.xr.tenant.dto.PlanUpgradeRequest;
import com.vcall.xr.tenant.dto.TenantRequest;
import com.vcall.xr.tenant.dto.TenantResponse;
import com.vcall.xr.tenant.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TenantResponse>> createTenant(@Valid @RequestBody TenantRequest request) {
        TenantResponse response = tenantService.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tenant created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TenantResponse>> getTenantById(@PathVariable UUID id) {
        TenantResponse response = tenantService.getTenantById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/by-slug/{slug}")
    public ResponseEntity<ApiResponse<TenantResponse>> getTenantBySlug(@PathVariable String slug) {
        TenantResponse response = tenantService.getTenantBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TenantResponse>>> getAllTenants(Pageable pageable) {
        Page<TenantResponse> response = tenantService.getAllTenants(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TenantResponse>> updateTenant(@PathVariable UUID id,
                                                                     @Valid @RequestBody TenantRequest request) {
        TenantResponse response = tenantService.updateTenant(id, request);
        return ResponseEntity.ok(ApiResponse.success("Tenant updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTenant(@PathVariable UUID id) {
        tenantService.deleteTenant(id);
        return ResponseEntity.ok(ApiResponse.success("Tenant deleted successfully", null));
    }

    @PatchMapping("/{id}/suspend")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TenantResponse>> suspendTenant(@PathVariable UUID id) {
        TenantResponse response = tenantService.suspendTenant(id);
        return ResponseEntity.ok(ApiResponse.success("Tenant suspended successfully", response));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TenantResponse>> activateTenant(@PathVariable UUID id) {
        TenantResponse response = tenantService.activateTenant(id);
        return ResponseEntity.ok(ApiResponse.success("Tenant activated successfully", response));
    }

    @PostMapping("/{id}/upgrade-plan")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TenantResponse>> upgradePlan(@PathVariable UUID id,
                                                                    @Valid @RequestBody PlanUpgradeRequest request) {
        TenantResponse response = tenantService.upgradePlan(id, request);
        return ResponseEntity.ok(ApiResponse.success("Plan upgraded successfully", response));
    }

    @GetMapping("/{tenantId}/feature-flags")
    public ResponseEntity<ApiResponse<List<FeatureFlagResponse>>> getFeatureFlags(@PathVariable UUID tenantId) {
        List<FeatureFlagResponse> response = tenantService.getFeatureFlags(tenantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{tenantId}/feature-flags/{featureKey}")
    public ResponseEntity<ApiResponse<FeatureFlagResponse>> getFeatureFlag(@PathVariable UUID tenantId,
                                                                            @PathVariable String featureKey) {
        FeatureFlagResponse response = tenantService.getFeatureFlag(tenantId, featureKey);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{tenantId}/feature-flags/{featureKey}/enabled")
    public ResponseEntity<ApiResponse<Boolean>> isFeatureEnabled(@PathVariable UUID tenantId,
                                                                  @PathVariable String featureKey) {
        boolean enabled = tenantService.isFeatureEnabled(tenantId, featureKey);
        return ResponseEntity.ok(ApiResponse.success(enabled));
    }

    @PostMapping("/{tenantId}/feature-flags")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<FeatureFlagResponse>> createFeatureFlag(
            @PathVariable UUID tenantId,
            @Valid @RequestBody FeatureFlagRequest request) {
        FeatureFlagResponse response = tenantService.createFeatureFlag(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Feature flag created successfully", response));
    }

    @PutMapping("/{tenantId}/feature-flags/{flagId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<FeatureFlagResponse>> updateFeatureFlag(
            @PathVariable UUID tenantId,
            @PathVariable UUID flagId,
            @Valid @RequestBody FeatureFlagRequest request) {
        FeatureFlagResponse response = tenantService.updateFeatureFlag(tenantId, flagId, request);
        return ResponseEntity.ok(ApiResponse.success("Feature flag updated successfully", response));
    }

    @DeleteMapping("/{tenantId}/feature-flags/{flagId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteFeatureFlag(@PathVariable UUID tenantId,
                                                                @PathVariable UUID flagId) {
        tenantService.deleteFeatureFlag(tenantId, flagId);
        return ResponseEntity.ok(ApiResponse.success("Feature flag deleted successfully", null));
    }
}
