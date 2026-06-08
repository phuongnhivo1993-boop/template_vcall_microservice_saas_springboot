package com.vcall.xr.twin.controller;

import com.vcall.xr.twin.dto.DigitalTwinMapper;
import com.vcall.xr.twin.dto.DigitalTwinRequest;
import com.vcall.xr.twin.dto.DigitalTwinResponse;
import com.vcall.xr.twin.service.DigitalTwinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/digital-twins")
@RequiredArgsConstructor
@Tag(name = "Digital Twin", description = "Digital twin sync and IoT integration")
public class DigitalTwinController {

    private final DigitalTwinService digitalTwinService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Create a digital twin")
    public ResponseEntity<DigitalTwinResponse> createDigitalTwin(@Valid @RequestBody DigitalTwinRequest request) {
        DigitalTwinResponse created = DigitalTwinMapper.toResponse(
                digitalTwinService.createDigitalTwin(DigitalTwinMapper.toEntity(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Get digital twin by ID")
    public ResponseEntity<DigitalTwinResponse> getDigitalTwin(@PathVariable UUID id) {
        return ResponseEntity.ok(DigitalTwinMapper.toResponse(digitalTwinService.getDigitalTwin(id)));
    }

    @GetMapping("/tenant/{tenantId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Get all digital twins for a tenant")
    public ResponseEntity<List<DigitalTwinResponse>> getByTenant(@PathVariable UUID tenantId) {
        List<DigitalTwinResponse> twins = digitalTwinService.getByTenant(tenantId).stream()
                .map(DigitalTwinMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(twins);
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Get digital twins by type")
    public ResponseEntity<List<DigitalTwinResponse>> getByType(@PathVariable String type) {
        List<DigitalTwinResponse> twins = digitalTwinService.getByType(type).stream()
                .map(DigitalTwinMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(twins);
    }

    @GetMapping("/scene/{sceneId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Get digital twins by scene ID")
    public ResponseEntity<List<DigitalTwinResponse>> getBySceneId(@PathVariable UUID sceneId) {
        List<DigitalTwinResponse> twins = digitalTwinService.getBySceneId(sceneId).stream()
                .map(DigitalTwinMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(twins);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Update digital twin")
    public ResponseEntity<DigitalTwinResponse> updateDigitalTwin(@PathVariable UUID id,
                                                                  @Valid @RequestBody DigitalTwinRequest request) {
        DigitalTwinResponse updated = DigitalTwinMapper.toResponse(
                digitalTwinService.updateDigitalTwin(id, DigitalTwinMapper.toEntity(request)));
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Delete digital twin")
    public ResponseEntity<Void> deleteDigitalTwin(@PathVariable UUID id) {
        digitalTwinService.deleteDigitalTwin(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/sync")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Sync twin data")
    public ResponseEntity<DigitalTwinResponse> syncTwinData(@PathVariable UUID id, @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(DigitalTwinMapper.toResponse(
                digitalTwinService.syncTwinData(id, payload.get("payload"))));
    }

    @PostMapping("/{id}/iot-endpoints")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Update IoT endpoints")
    public ResponseEntity<Void> updateIoTEndpoints(@PathVariable UUID id, @RequestBody Map<String, String> payload) {
        digitalTwinService.updateIoTEndpoints(id, payload.get("endpoints"));
        return ResponseEntity.ok().build();
    }
}
