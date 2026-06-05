package com.vcall.xr.scene.repository;

import com.vcall.xr.scene.domain.Scene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SceneRepository extends JpaRepository<Scene, UUID>, JpaSpecificationExecutor<Scene> {

    List<Scene> findByTenantId(UUID tenantId);

    List<Scene> findByTenantIdAndStatus(UUID tenantId, String status);

    long countByTenantId(UUID tenantId);
}
