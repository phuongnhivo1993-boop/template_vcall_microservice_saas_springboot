package com.vcall.xr.tenant.repository;

import com.vcall.xr.tenant.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findTopByTenantIdOrderByCreatedAtDesc(UUID tenantId);
}
