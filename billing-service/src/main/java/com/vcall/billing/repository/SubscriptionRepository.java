package com.vcall.billing.repository;

import com.vcall.billing.entity.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findBySubscriberId(UUID subscriberId);
    Page<Subscription> findBySubscriberId(UUID subscriberId, Pageable pageable);

    List<Subscription> findByPlanId(Long planId);

    List<Subscription> findByStatus(Subscription.SubscriptionStatus status);
    Page<Subscription> findByStatus(Subscription.SubscriptionStatus status, Pageable pageable);

    List<Subscription> findByEndDateBefore(LocalDateTime date);

    List<Subscription> findByStatusAndEndDateBetween(Subscription.SubscriptionStatus status, LocalDateTime start, LocalDateTime end);
}
