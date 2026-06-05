package com.vcall.xr.gis.repository;

import com.vcall.xr.gis.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    List<Room> findByFloorId(UUID floorId);

    @Query(value = "SELECT r FROM Room r WHERE r.floorId = :floorId " +
            "AND ST_Contains(r.boundaries, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)) = true")
    List<Room> findRoomByPoint(@Param("floorId") UUID floorId,
                               @Param("latitude") double latitude,
                               @Param("longitude") double longitude);

    @Query(value = "SELECT r FROM Room r WHERE ST_DWithin(r.center_point, " +
            "ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), :distanceMeters) = true")
    List<Room> findNearbyRooms(@Param("latitude") double latitude,
                               @Param("longitude") double longitude,
                               @Param("distanceMeters") double distanceMeters);
}
