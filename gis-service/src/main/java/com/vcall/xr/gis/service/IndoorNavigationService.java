package com.vcall.xr.gis.service;

import com.vcall.xr.gis.domain.Floor;
import com.vcall.xr.gis.domain.Room;
import com.vcall.xr.gis.repository.FloorRepository;
import com.vcall.xr.gis.repository.PathRepository;
import com.vcall.xr.gis.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndoorNavigationService {

    private final FloorRepository floorRepository;
    private final RoomRepository roomRepository;
    private final PathRepository pathRepository;

    @Transactional
    public Floor createFloor(Floor floor) {
        floor.setCreatedAt(LocalDateTime.now());
        floor.setUpdatedAt(LocalDateTime.now());
        floor.setIsDeleted(false);
        Floor saved = floorRepository.save(floor);
        log.info("Created floor {} for tenant {}", saved.getId(), saved.getTenantId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Floor getFloor(UUID floorId) {
        return floorRepository.findById(floorId)
                .orElseThrow(() -> new RuntimeException("Floor not found with id: " + floorId));
    }

    @Transactional(readOnly = true)
    public List<Floor> getFloorsByBuilding(UUID buildingId) {
        return floorRepository.findByBuildingIdOrderByLevelAsc(buildingId);
    }

    @Transactional(readOnly = true)
    public List<Floor> getFloorsByTenant(UUID tenantId) {
        return floorRepository.findByTenantId(tenantId);
    }

    @Transactional
    public Floor updateFloor(UUID floorId, Floor update) {
        Floor floor = getFloor(floorId);
        floor.setName(update.getName());
        floor.setLevel(update.getLevel());
        floor.setFloorPlanAssetId(update.getFloorPlanAssetId());
        floor.setUpdatedAt(LocalDateTime.now());
        return floorRepository.save(floor);
    }

    @Transactional
    public void deleteFloor(UUID floorId) {
        Floor floor = getFloor(floorId);
        floor.setIsDeleted(true);
        floor.setUpdatedAt(LocalDateTime.now());
        floorRepository.save(floor);
        log.info("Deleted floor {}", floorId);
    }

    @Transactional
    public Room createRoom(Room room) {
        room.setCreatedAt(LocalDateTime.now());
        room.setUpdatedAt(LocalDateTime.now());
        Room saved = roomRepository.save(room);
        log.info("Created room {} on floor {}", saved.getId(), saved.getFloorId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Room getRoom(UUID roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));
    }

    @Transactional(readOnly = true)
    public List<Room> getRoomsByFloor(UUID floorId) {
        return roomRepository.findByFloorId(floorId);
    }

    @Transactional
    public Room updateRoom(UUID roomId, Room update) {
        Room room = getRoom(roomId);
        room.setName(update.getName());
        room.setBoundaries(update.getBoundaries());
        room.setCenterPoint(update.getCenterPoint());
        room.setUpdatedAt(LocalDateTime.now());
        return roomRepository.save(room);
    }

    @Transactional
    public void deleteRoom(UUID roomId) {
        Room room = getRoom(roomId);
        roomRepository.delete(room);
        log.info("Deleted room {}", roomId);
    }

    public List<Room> findRoomByLocation(UUID floorId, double latitude, double longitude) {
        return roomRepository.findRoomByPoint(floorId, latitude, longitude);
    }

    public List<Room> findNearbyRooms(double latitude, double longitude, double radiusMeters) {
        return roomRepository.findNearbyRooms(latitude, longitude, radiusMeters);
    }

    public Map<String, Object> getIndoorNavigationContext(UUID floorId, double latitude, double longitude) {
        Floor floor = getFloor(floorId);
        List<Room> nearbyRooms = findRoomByLocation(floorId, latitude, longitude);

        Map<String, Object> context = new HashMap<>();
        context.put("floor", Map.of(
                "id", floor.getId(),
                "name", floor.getName(),
                "level", floor.getLevel(),
                "buildingId", floor.getBuildingId()
        ));
        context.put("currentLocation", Map.of(
                "latitude", latitude,
                "longitude", longitude
        ));
        context.put("nearbyRooms", nearbyRooms.stream()
                .map(r -> Map.of("id", r.getId(), "name", r.getName()))
                .toList());
        context.put("totalRoomsOnFloor", getRoomsByFloor(floorId).size());

        return context;
    }

    public Map<String, Object> getBuildingMap(UUID buildingId) {
        List<Floor> floors = getFloorsByBuilding(buildingId);
        Map<String, Object> buildingMap = new HashMap<>();
        buildingMap.put("buildingId", buildingId);
        buildingMap.put("floors", floors.stream()
                .map(f -> Map.of(
                        "id", f.getId(),
                        "name", f.getName(),
                        "level", f.getLevel(),
                        "floorPlanAssetId", f.getFloorPlanAssetId() != null ? f.getFloorPlanAssetId().toString() : null
                ))
                .toList());
        return buildingMap;
    }
}
