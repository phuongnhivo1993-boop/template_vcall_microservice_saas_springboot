package com.vcall.xr.tenant.repository;

import com.vcall.xr.tenant.domain.FeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, UUID> {

    List<FeatureFlag> findByTenantId(UUID tenantId);

    Optional<FeatureFlag> findByTenantIdAndFeatureKey(UUID tenantId, String featureKey);

    boolean existsByTenantIdAndFeatureKey(UUID tenantId, String featureKey);
}
