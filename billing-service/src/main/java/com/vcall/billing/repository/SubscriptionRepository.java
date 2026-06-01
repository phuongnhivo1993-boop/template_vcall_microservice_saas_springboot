package com.vcall.billing.repository;

import com.vcall.billing.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findBySubscriberId(UUID subscriberId);

    List<Subscription> findByPlanId(Long planId);

    List<Subscription> findByStatus(Subscription.SubscriptionStatus status);

    List<Subscription> findByEndDateBefore(LocalDateTime date);

    List<Subscription> findByStatusAndEndDateBetween(Subscription.SubscriptionStatus status, LocalDateTime start, LocalDateTime end);
}
