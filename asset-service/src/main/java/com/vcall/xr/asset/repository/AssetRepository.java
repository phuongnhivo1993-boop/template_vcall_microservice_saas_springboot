package com.vcall.xr.asset.repository;

import com.vcall.xr.asset.domain.Asset;
import com.vcall.xr.asset.domain.AssetType;
import com.vcall.xr.asset.domain.TranscodeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssetRepository extends JpaRepository<Asset, UUID>, JpaSpecificationExecutor<Asset> {

    Page<Asset> findByTenantId(UUID tenantId, Pageable pageable);

    Page<Asset> findByTenantIdAndType(UUID tenantId, AssetType type, Pageable pageable);

    List<Asset> findByTranscodeStatus(TranscodeStatus status);

    long countByTenantIdAndType(UUID tenantId, AssetType type);

    boolean existsByTenantIdAndId(UUID tenantId, UUID id);
}
