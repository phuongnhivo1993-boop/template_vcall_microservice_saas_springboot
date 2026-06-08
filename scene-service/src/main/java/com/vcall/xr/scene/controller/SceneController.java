package com.vcall.xr.scene.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.common.util.BulkOperationUtil;
import com.vcall.xr.scene.domain.Scene;
import com.vcall.xr.scene.dto.HotspotRequest;
import com.vcall.xr.scene.dto.HotspotResponse;
import com.vcall.xr.scene.dto.SceneNodeRequest;
import com.vcall.xr.scene.dto.SceneNodeResponse;
import com.vcall.xr.scene.dto.SceneRequest;
import com.vcall.xr.scene.dto.SceneResponse;
import com.vcall.xr.scene.service.HotspotService;
import com.vcall.xr.scene.service.SceneGraphService;
import com.vcall.xr.scene.service.SceneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/scenes")
@RequiredArgsConstructor
public class SceneController {

    private final SceneService sceneService;
    private final SceneGraphService sceneGraphService;
    private final HotspotService hotspotService;

    @PostMapping
    public ResponseEntity<ApiResponse<SceneResponse>> createScene(@Valid @RequestBody SceneRequest request) {
        SceneResponse response = sceneService.createScene(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Scene created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SceneResponse>> getSceneById(@PathVariable UUID id) {
        SceneResponse response = sceneService.getSceneById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SceneResponse>>> getAllScenes(Pageable pageable) {
        Page<SceneResponse> response = sceneService.getAllScenes(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<SceneResponse>>> searchScenes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID tenantId,
            Pageable pageable) {
        Specification<Scene> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%")
                    ));
        }
        if (type != null && !type.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("type").as(String.class)), type.toLowerCase()));
        }
        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("status").as(String.class)), status.toLowerCase()));
        }
        if (tenantId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("tenantId"), tenantId));
        }
        Page<SceneResponse> response = sceneService.searchScenes(spec, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<ApiResponse<List<SceneResponse>>> getScenesByTenant(@PathVariable UUID tenantId) {
        List<SceneResponse> response = sceneService.getScenesByTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/tenant/{tenantId}/status/{status}")
    public ResponseEntity<ApiResponse<List<SceneResponse>>> getScenesByTenantAndStatus(
            @PathVariable UUID tenantId, @PathVariable String status) {
        List<SceneResponse> response = sceneService.getScenesByTenantAndStatus(tenantId, status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SceneResponse>> updateScene(
            @PathVariable UUID id, @Valid @RequestBody SceneRequest request) {
        SceneResponse response = sceneService.updateScene(id, request);
        return ResponseEntity.ok(ApiResponse.success("Scene updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteScene(@PathVariable UUID id) {
        sceneService.deleteScene(id);
        return ResponseEntity.ok(ApiResponse.success("Scene deleted successfully", null));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<SceneResponse>> publishScene(@PathVariable UUID id) {
        SceneResponse response = sceneService.publishScene(id);
        return ResponseEntity.ok(ApiResponse.success("Scene published successfully", response));
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<ApiResponse<SceneResponse>> unpublishScene(@PathVariable UUID id) {
        SceneResponse response = sceneService.unpublishScene(id);
        return ResponseEntity.ok(ApiResponse.success("Scene unpublished successfully", response));
    }

    @PostMapping("/{id}/duplicate")
    public ResponseEntity<ApiResponse<SceneResponse>> duplicateScene(
            @PathVariable UUID id,
            @RequestParam(required = false) String newName) {
        SceneResponse response = sceneService.duplicateScene(id, newName);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Scene duplicated successfully", response));
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<ApiResponse<SceneResponse>> incrementViewCount(@PathVariable UUID id) {
        SceneResponse response = sceneService.incrementViewCount(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{sceneId}/nodes")
    public ResponseEntity<ApiResponse<SceneNodeResponse>> addNode(
            @PathVariable UUID sceneId, @Valid @RequestBody SceneNodeRequest request) {
        SceneNodeResponse response = sceneGraphService.addNode(sceneId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Node added successfully", response));
    }

    @GetMapping("/{sceneId}/nodes")
    public ResponseEntity<ApiResponse<List<SceneNodeResponse>>> getNodes(@PathVariable UUID sceneId) {
        List<SceneNodeResponse> response = sceneGraphService.getNodesByScene(sceneId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{sceneId}/nodes/{nodeId}")
    public ResponseEntity<ApiResponse<SceneNodeResponse>> getNode(
            @PathVariable UUID sceneId, @PathVariable UUID nodeId) {
        SceneNodeResponse response = sceneGraphService.getNodeById(nodeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{sceneId}/nodes/{parentId}/children")
    public ResponseEntity<ApiResponse<List<SceneNodeResponse>>> getChildNodes(
            @PathVariable UUID sceneId, @PathVariable UUID parentId) {
        List<SceneNodeResponse> response = sceneGraphService.getChildNodes(sceneId, parentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{sceneId}/nodes/{nodeId}")
    public ResponseEntity<ApiResponse<SceneNodeResponse>> updateNode(
            @PathVariable UUID sceneId,
            @PathVariable UUID nodeId,
            @Valid @RequestBody SceneNodeRequest request) {
        SceneNodeResponse response = sceneGraphService.updateNode(nodeId, request);
        return ResponseEntity.ok(ApiResponse.success("Node updated successfully", response));
    }

    @DeleteMapping("/{sceneId}/nodes/{nodeId}")
    public ResponseEntity<ApiResponse<Void>> deleteNode(
            @PathVariable UUID sceneId, @PathVariable UUID nodeId) {
        sceneGraphService.deleteNode(nodeId);
        return ResponseEntity.ok(ApiResponse.success("Node deleted successfully", null));
    }

    @PutMapping("/{sceneId}/nodes/reorder")
    public ResponseEntity<ApiResponse<List<SceneNodeResponse>>> reorderNodes(
            @PathVariable UUID sceneId, @RequestBody List<UUID> nodeOrder) {
        List<SceneNodeResponse> response = sceneGraphService.reorderNodes(sceneId, nodeOrder);
        return ResponseEntity.ok(ApiResponse.success("Nodes reordered successfully", response));
    }

    @PostMapping("/{sceneId}/hotspots")
    public ResponseEntity<ApiResponse<HotspotResponse>> createHotspot(
            @PathVariable UUID sceneId, @Valid @RequestBody HotspotRequest request) {
        HotspotResponse response = hotspotService.createHotspot(sceneId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Hotspot created successfully", response));
    }

    @GetMapping("/{sceneId}/hotspots")
    public ResponseEntity<ApiResponse<List<HotspotResponse>>> getHotspots(@PathVariable UUID sceneId) {
        List<HotspotResponse> response = hotspotService.getHotspotsByScene(sceneId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{sceneId}/hotspots/{hotspotId}")
    public ResponseEntity<ApiResponse<HotspotResponse>> getHotspot(
            @PathVariable UUID sceneId, @PathVariable UUID hotspotId) {
        HotspotResponse response = hotspotService.getHotspotById(hotspotId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{sceneId}/hotspots/node/{nodeId}")
    public ResponseEntity<ApiResponse<List<HotspotResponse>>> getHotspotsByNode(
            @PathVariable UUID sceneId, @PathVariable UUID nodeId) {
        List<HotspotResponse> response = hotspotService.getHotspotsByNode(nodeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{sceneId}/hotspots/{hotspotId}")
    public ResponseEntity<ApiResponse<HotspotResponse>> updateHotspot(
            @PathVariable UUID sceneId,
            @PathVariable UUID hotspotId,
            @Valid @RequestBody HotspotRequest request) {
        HotspotResponse response = hotspotService.updateHotspot(hotspotId, request);
        return ResponseEntity.ok(ApiResponse.success("Hotspot updated successfully", response));
    }

    @DeleteMapping("/{sceneId}/hotspots/{hotspotId}")
    public ResponseEntity<ApiResponse<Void>> deleteHotspot(
            @PathVariable UUID sceneId, @PathVariable UUID hotspotId) {
        hotspotService.deleteHotspot(hotspotId);
        return ResponseEntity.ok(ApiResponse.success("Hotspot deleted successfully", null));
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<SceneResponse>> restoreScene(@PathVariable UUID id) {
        SceneResponse response = sceneService.restoreScene(id);
        return ResponseEntity.ok(ApiResponse.success("Scene restored successfully", response));
    }

    @PostMapping("/restore-bulk")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<BulkOperationUtil.BulkResult<UUID>>> restoreBulk(
            @RequestBody List<UUID> ids) {
        BulkOperationUtil.BulkResult<UUID> result = new BulkOperationUtil.BulkResult<>();
        for (UUID id : ids) {
            try {
                sceneService.restoreScene(id);
                result.addSuccess(id);
            } catch (Exception e) {
                result.addFailure(id, e.getMessage());
            }
        }
        return ResponseEntity.ok(ApiResponse.success("Bulk restore completed", result));
    }
}
