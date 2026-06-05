package com.vcall.xr.twin.controller;

import com.vcall.xr.twin.domain.DigitalTwin;
import com.vcall.xr.twin.service.DigitalTwinService;
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
@RequestMapping("/api/v1/digital-twins")
@RequiredArgsConstructor
@Tag(name = "Digital Twin", description = "Digital twin sync and IoT integration")
public class DigitalTwinController {

    private final DigitalTwinService digitalTwinService;

    @PostMapping
    @Operation(summary = "Create a digital twin")
    public ResponseEntity<DigitalTwin> createDigitalTwin(@RequestBody DigitalTwin twin) {
        DigitalTwin created = digitalTwinService.createDigitalTwin(twin);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get digital twin by ID")
    public ResponseEntity<DigitalTwin> getDigitalTwin(@PathVariable UUID id) {
        return ResponseEntity.ok(digitalTwinService.getDigitalTwin(id));
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get all digital twins for a tenant")
    public ResponseEntity<List<DigitalTwin>> getByTenant(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(digitalTwinService.getByTenant(tenantId));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get digital twins by type")
    public ResponseEntity<List<DigitalTwin>> getByType(@PathVariable String type) {
        return ResponseEntity.ok(digitalTwinService.getByType(type));
    }

    @GetMapping("/scene/{sceneId}")
    @Operation(summary = "Get digital twins by scene ID")
    public ResponseEntity<List<DigitalTwin>> getBySceneId(@PathVariable UUID sceneId) {
        return ResponseEntity.ok(digitalTwinService.getBySceneId(sceneId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update digital twin")
    public ResponseEntity<DigitalTwin> updateDigitalTwin(@PathVariable UUID id, @RequestBody DigitalTwin twin) {
        return ResponseEntity.ok(digitalTwinService.updateDigitalTwin(id, twin));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete digital twin")
    public ResponseEntity<Void> deleteDigitalTwin(@PathVariable UUID id) {
        digitalTwinService.deleteDigitalTwin(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/sync")
    @Operation(summary = "Sync twin data")
    public ResponseEntity<DigitalTwin> syncTwinData(@PathVariable UUID id, @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(digitalTwinService.syncTwinData(id, payload.get("payload")));
    }

    @PostMapping("/{id}/iot-endpoints")
    @Operation(summary = "Update IoT endpoints")
    public ResponseEntity<Void> updateIoTEndpoints(@PathVariable UUID id, @RequestBody Map<String, String> payload) {
        digitalTwinService.updateIoTEndpoints(id, payload.get("endpoints"));
        return ResponseEntity.ok().build();
    }
}
