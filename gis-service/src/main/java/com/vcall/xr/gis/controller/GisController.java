package com.vcall.xr.gis.controller;

import com.vcall.xr.gis.domain.Floor;
import com.vcall.xr.gis.domain.Room;
import com.vcall.xr.gis.service.IndoorNavigationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/gis")
@RequiredArgsConstructor
@Tag(name = "GIS", description = "Indoor navigation, GIS map, and spatial queries")
public class GisController {

    private final IndoorNavigationService navigationService;

    @PostMapping("/floors")
    @Operation(summary = "Create a floor")
    public ResponseEntity<Floor> createFloor(@RequestBody Floor floor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(navigationService.createFloor(floor));
    }

    @GetMapping("/floors/{floorId}")
    @Operation(summary = "Get floor by ID")
    public ResponseEntity<Floor> getFloor(@PathVariable UUID floorId) {
        return ResponseEntity.ok(navigationService.getFloor(floorId));
    }

    @GetMapping("/floors/building/{buildingId}")
    @Operation(summary = "Get floors by building")
    public ResponseEntity<List<Floor>> getFloorsByBuilding(@PathVariable UUID buildingId) {
        return ResponseEntity.ok(navigationService.getFloorsByBuilding(buildingId));
    }

    @GetMapping("/floors/tenant/{tenantId}")
    @Operation(summary = "Get floors by tenant")
    public ResponseEntity<List<Floor>> getFloorsByTenant(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(navigationService.getFloorsByTenant(tenantId));
    }

    @PutMapping("/floors/{floorId}")
    @Operation(summary = "Update floor")
    public ResponseEntity<Floor> updateFloor(@PathVariable UUID floorId, @RequestBody Floor floor) {
        return ResponseEntity.ok(navigationService.updateFloor(floorId, floor));
    }

    @DeleteMapping("/floors/{floorId}")
    @Operation(summary = "Delete floor")
    public ResponseEntity<Void> deleteFloor(@PathVariable UUID floorId) {
        navigationService.deleteFloor(floorId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/rooms")
    @Operation(summary = "Create a room")
    public ResponseEntity<Room> createRoom(@RequestBody Room room) {
        return ResponseEntity.status(HttpStatus.CREATED).body(navigationService.createRoom(room));
    }

    @GetMapping("/rooms/{roomId}")
    @Operation(summary = "Get room by ID")
    public ResponseEntity<Room> getRoom(@PathVariable UUID roomId) {
        return ResponseEntity.ok(navigationService.getRoom(roomId));
    }

    @GetMapping("/rooms/floor/{floorId}")
    @Operation(summary = "Get rooms by floor")
    public ResponseEntity<List<Room>> getRoomsByFloor(@PathVariable UUID floorId) {
        return ResponseEntity.ok(navigationService.getRoomsByFloor(floorId));
    }

    @PutMapping("/rooms/{roomId}")
    @Operation(summary = "Update room")
    public ResponseEntity<Room> updateRoom(@PathVariable UUID roomId, @RequestBody Room room) {
        return ResponseEntity.ok(navigationService.updateRoom(roomId, room));
    }

    @DeleteMapping("/rooms/{roomId}")
    @Operation(summary = "Delete room")
    public ResponseEntity<Void> deleteRoom(@PathVariable UUID roomId) {
        navigationService.deleteRoom(roomId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/rooms/locate")
    @Operation(summary = "Find room by GPS coordinates on a floor")
    public ResponseEntity<List<Room>> findRoomByLocation(
            @RequestParam UUID floorId,
            @RequestParam double latitude,
            @RequestParam double longitude) {
        return ResponseEntity.ok(navigationService.findRoomByLocation(floorId, latitude, longitude));
    }

    @GetMapping("/rooms/nearby")
    @Operation(summary = "Find nearby rooms by GPS coordinates")
    public ResponseEntity<List<Room>> findNearbyRooms(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "100") double radiusMeters) {
        return ResponseEntity.ok(navigationService.findNearbyRooms(latitude, longitude, radiusMeters));
    }

    @GetMapping("/navigation/context")
    @Operation(summary = "Get indoor navigation context for a location")
    public ResponseEntity<Map<String, Object>> getNavigationContext(
            @RequestParam UUID floorId,
            @RequestParam double latitude,
            @RequestParam double longitude) {
        return ResponseEntity.ok(navigationService.getIndoorNavigationContext(floorId, latitude, longitude));
    }

    @GetMapping("/building/{buildingId}/map")
    @Operation(summary = "Get building map with all floors")
    public ResponseEntity<Map<String, Object>> getBuildingMap(@PathVariable UUID buildingId) {
        return ResponseEntity.ok(navigationService.getBuildingMap(buildingId));
    }
}
