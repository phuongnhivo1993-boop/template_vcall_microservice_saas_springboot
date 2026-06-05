package com.vcall.xr.twin.repository;

import com.vcall.xr.twin.domain.DigitalTwin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DigitalTwinRepository extends JpaRepository<DigitalTwin, UUID> {

    List<DigitalTwin> findByTenantId(UUID tenantId);

    List<DigitalTwin> findByType(String type);

    List<DigitalTwin> findBySceneId(UUID sceneId);

    List<DigitalTwin> findByBimAssetId(UUID bimAssetId);
}
