package com.vcall.xr.gis.repository;

import com.vcall.xr.gis.domain.Floor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FloorRepository extends JpaRepository<Floor, UUID> {

    List<Floor> findByTenantId(UUID tenantId);

    List<Floor> findByBuildingId(UUID buildingId);

    List<Floor> findByTenantIdAndBuildingId(UUID tenantId, UUID buildingId);

    List<Floor> findByBuildingIdOrderByLevelAsc(UUID buildingId);
}
