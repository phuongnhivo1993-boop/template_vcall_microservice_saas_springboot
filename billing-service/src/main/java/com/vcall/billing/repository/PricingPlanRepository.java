package com.vcall.billing.repository;

import com.vcall.billing.entity.PricingPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface PricingPlanRepository extends JpaRepository<PricingPlan, Long>, JpaSpecificationExecutor<PricingPlan> {

    List<PricingPlan> findByPlanType(PricingPlan.PlanType planType);
    Page<PricingPlan> findByPlanType(PricingPlan.PlanType planType, Pageable pageable);

    List<PricingPlan> findByIsActiveTrue();
    Page<PricingPlan> findByIsActiveTrue(Pageable pageable);

    Optional<PricingPlan> findByName(String name);
}
