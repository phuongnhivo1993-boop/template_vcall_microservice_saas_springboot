package com.vcall.billing.service;

import com.vcall.billing.dto.PricingPlanRequest;
import com.vcall.billing.dto.PricingPlanResponse;
import com.vcall.billing.entity.PricingPlan;
import com.vcall.billing.repository.PricingPlanRepository;
import com.vcall.common.exception.DuplicateResourceException;
import com.vcall.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PricingPlanService {

    private final PricingPlanRepository pricingPlanRepository;

    @Transactional
    public PricingPlanResponse createPlan(PricingPlanRequest request) {
        if (pricingPlanRepository.findByName(request.getName()).isPresent()) {
            throw new DuplicateResourceException("Pricing plan already exists with name: " + request.getName());
        }
        PricingPlan plan = new PricingPlan();
        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setPlanType(request.getPlanType());
        plan.setPrice(request.getPrice());
        plan.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        plan.setBillingCycle(request.getBillingCycle());
        plan.setFeatures(request.getFeatures());
        plan.setActive(request.getIsActive() != null ? request.getIsActive() : true);
        plan = pricingPlanRepository.save(plan);
        return toResponse(plan);
    }

    @Transactional
    public PricingPlanResponse updatePlan(Long id, PricingPlanRequest request) {
        PricingPlan plan = pricingPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing plan not found with id: " + id));
        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setPlanType(request.getPlanType());
        plan.setPrice(request.getPrice());
        plan.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        plan.setBillingCycle(request.getBillingCycle());
        plan.setFeatures(request.getFeatures());
        plan.setActive(request.getIsActive() != null ? request.getIsActive() : plan.isActive());
        plan = pricingPlanRepository.save(plan);
        return toResponse(plan);
    }

    @Transactional(readOnly = true)
    public PricingPlanResponse getPlan(Long id) {
        PricingPlan plan = pricingPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing plan not found with id: " + id));
        return toResponse(plan);
    }

    @Transactional(readOnly = true)
    public Page<PricingPlanResponse> getAllPlans(Pageable pageable) {
        return pricingPlanRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PricingPlanResponse> getActivePlans(Pageable pageable) {
        return pricingPlanRepository.findByIsActiveTrue(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PricingPlanResponse> getPlansByType(PricingPlan.PlanType planType, Pageable pageable) {
        return pricingPlanRepository.findByPlanType(planType, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public void deletePlan(Long id) {
        PricingPlan plan = pricingPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing plan not found with id: " + id));
        plan.setIsDeleted(true);
        pricingPlanRepository.save(plan);
    }

    private PricingPlanResponse toResponse(PricingPlan plan) {
        return PricingPlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .planType(plan.getPlanType())
                .price(plan.getPrice())
                .currency(plan.getCurrency())
                .billingCycle(plan.getBillingCycle())
                .features(plan.getFeatures())
                .isActive(plan.isActive())
                .build();
    }
}
