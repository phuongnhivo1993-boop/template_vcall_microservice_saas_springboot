package com.vcall.xr.gis.repository;

import com.vcall.xr.gis.domain.Floor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PathRepository extends JpaRepository<Floor, UUID> {

    @Query(value = "SELECT * FROM xr_path WHERE floor_id = :floorId", nativeQuery = true)
    List<Object[]> findPathsByFloorId(@Param("floorId") UUID floorId);

    @Query(value = "SELECT * FROM xr_path WHERE floor_id = :floorId " +
            "AND ST_DWithin(path, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), :distanceMeters) = true",
            nativeQuery = true)
    List<Object[]> findNearbyPaths(@Param("floorId") UUID floorId,
                                   @Param("latitude") double latitude,
                                   @Param("longitude") double longitude,
                                   @Param("distanceMeters") double distanceMeters);
}
