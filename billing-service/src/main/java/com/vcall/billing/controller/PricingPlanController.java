package com.vcall.billing.controller;

import com.vcall.billing.dto.PricingPlanRequest;
import com.vcall.billing.dto.PricingPlanResponse;
import com.vcall.billing.entity.PricingPlan;
import com.vcall.billing.service.PricingPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/api/v1/billing/plans")
@RequiredArgsConstructor
public class PricingPlanController {

    private final PricingPlanService pricingPlanService;

    @PostMapping
    public ResponseEntity<PricingPlanResponse> createPlan(@Valid @RequestBody PricingPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pricingPlanService.createPlan(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PricingPlanResponse> updatePlan(@PathVariable Long id,
                                                          @Valid @RequestBody PricingPlanRequest request) {
        return ResponseEntity.ok(pricingPlanService.updatePlan(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PricingPlanResponse> getPlan(@PathVariable Long id) {
        return ResponseEntity.ok(pricingPlanService.getPlan(id));
    }

    @GetMapping
    public ResponseEntity<List<PricingPlanResponse>> getAllPlans(
            @RequestParam(required = false) PricingPlan.PlanType planType) {
        if (planType != null) {
            return ResponseEntity.ok(pricingPlanService.getPlansByType(planType));
        }
        return ResponseEntity.ok(pricingPlanService.getAllPlans());
    }

    @GetMapping("/active")
    public ResponseEntity<List<PricingPlanResponse>> getActivePlans() {
        return ResponseEntity.ok(pricingPlanService.getActivePlans());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable Long id) {
        pricingPlanService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }
}
