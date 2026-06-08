package com.vcall.billing.controller;

import com.vcall.billing.dto.PricingPlanRequest;
import com.vcall.billing.dto.PricingPlanResponse;
import com.vcall.billing.entity.PricingPlan;
import com.vcall.billing.service.PricingPlanService;
import com.vcall.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/billing/plans")
@RequiredArgsConstructor
public class PricingPlanController {

    private final PricingPlanService pricingPlanService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<PricingPlanResponse> createPlan(@Valid @RequestBody PricingPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pricingPlanService.createPlan(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<PricingPlanResponse> updatePlan(@PathVariable Long id,
                                                          @Valid @RequestBody PricingPlanRequest request) {
        return ResponseEntity.ok(pricingPlanService.updatePlan(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<PricingPlanResponse> getPlan(@PathVariable Long id) {
        return ResponseEntity.ok(pricingPlanService.getPlan(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Page<PricingPlanResponse>> getAllPlans(
            @RequestParam(required = false) PricingPlan.PlanType planType,
            Pageable pageable) {
        if (planType != null) {
            return ResponseEntity.ok(pricingPlanService.getPlansByType(planType, pageable));
        }
        return ResponseEntity.ok(pricingPlanService.getAllPlans(pageable));
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PricingPlanResponse>> getActivePlans(Pageable pageable) {
        return ResponseEntity.ok(pricingPlanService.getActivePlans(pageable));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Void> deletePlan(@PathVariable Long id) {
        pricingPlanService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk-delete")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Void>> bulkDeletePlans(@RequestBody List<Long> ids) {
        pricingPlanService.bulkDeletePlans(ids);
        return ResponseEntity.ok(ApiResponse.success("Plans deleted successfully", null));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<PricingPlanResponse>>> searchPlans(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) PricingPlan.PlanType planType,
            @RequestParam(required = false) Boolean isActive,
            Pageable pageable) {
        Page<PricingPlanResponse> result = pricingPlanService.searchPlans(name, planType, isActive, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPlanStats() {
        Map<String, Object> stats = pricingPlanService.getPlanStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
