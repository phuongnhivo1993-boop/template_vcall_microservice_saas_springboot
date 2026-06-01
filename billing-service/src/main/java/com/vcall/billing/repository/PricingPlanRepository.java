package com.vcall.billing.repository;

import com.vcall.billing.entity.PricingPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PricingPlanRepository extends JpaRepository<PricingPlan, Long> {

    List<PricingPlan> findByPlanType(PricingPlan.PlanType planType);

    List<PricingPlan> findByIsActiveTrue();

    Optional<PricingPlan> findByName(String name);
}
