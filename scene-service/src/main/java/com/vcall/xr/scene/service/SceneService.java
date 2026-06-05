package com.vcall.xr.scene.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.xr.scene.domain.Hotspot;
import com.vcall.xr.scene.domain.PublishStatus;
import com.vcall.xr.scene.domain.Scene;
import com.vcall.xr.scene.domain.SceneNode;
import com.vcall.xr.scene.dto.SceneRequest;
import com.vcall.xr.scene.dto.SceneResponse;
import com.vcall.xr.scene.mapper.SceneMapper;
import com.vcall.xr.scene.repository.HotspotRepository;
import com.vcall.xr.scene.repository.SceneNodeRepository;
import com.vcall.xr.scene.repository.SceneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SceneService {

    private final SceneRepository sceneRepository;
    private final SceneNodeRepository sceneNodeRepository;
    private final HotspotRepository hotspotRepository;
    private final SceneMapper sceneMapper;
    private final SceneEventPublisher eventPublisher;

    @Transactional
    public SceneResponse createScene(SceneRequest request) {
        Scene scene = sceneMapper.toEntity(request);
        if (scene.getTenantId() == null) {
            scene.setTenantId(UUID.randomUUID());
        }
        scene = sceneRepository.save(scene);
        log.info("Scene created: {}", scene.getId());
        eventPublisher.publishSceneCreated(scene);
        return toSceneResponse(scene);
    }

    @Transactional(readOnly = true)
    public SceneResponse getSceneById(UUID id) {
        Scene scene = sceneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scene not found with id: " + id));
        return toSceneResponse(scene);
    }

    @Transactional(readOnly = true)
    public Page<SceneResponse> getAllScenes(Pageable pageable) {
        return sceneRepository.findAll(pageable).map(this::toSceneResponse);
    }

    @Transactional(readOnly = true)
    public Page<SceneResponse> searchScenes(Specification<Scene> spec, Pageable pageable) {
        return sceneRepository.findAll(spec, pageable).map(this::toSceneResponse);
    }

    @Transactional(readOnly = true)
    public List<SceneResponse> getScenesByTenant(UUID tenantId) {
        return sceneRepository.findByTenantId(tenantId).stream()
                .map(this::toSceneResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SceneResponse> getScenesByTenantAndStatus(UUID tenantId, String status) {
        return sceneRepository.findByTenantIdAndStatus(tenantId, status).stream()
                .map(this::toSceneResponse)
                .toList();
    }

    @Transactional
    public SceneResponse updateScene(UUID id, SceneRequest request) {
        Scene scene = sceneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scene not found with id: " + id));
        sceneMapper.updateEntity(request, scene);
        scene = sceneRepository.save(scene);
        log.info("Scene updated: {}", scene.getId());
        eventPublisher.publishSceneUpdated(scene);
        return toSceneResponse(scene);
    }

    @Transactional
    public void deleteScene(UUID id) {
        Scene scene = sceneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scene not found with id: " + id));
        scene.setIsDeleted(true);
        sceneRepository.save(scene);
        log.info("Scene deleted (soft): {}", id);
        eventPublisher.publishSceneDeleted(id);
    }

    @Transactional
    public SceneResponse publishScene(UUID id) {
        Scene scene = sceneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scene not found with id: " + id));
        scene.setStatus(PublishStatus.PUBLISHED);
        scene.setPublishedAt(LocalDateTime.now());
        scene.setVersion(scene.getVersion() + 1);
        scene.setPublishedUrl("/scenes/" + scene.getId() + "/v" + scene.getVersion());
        scene = sceneRepository.save(scene);
        log.info("Scene published: {} version {}", scene.getId(), scene.getVersion());
        eventPublisher.publishScenePublished(scene);
        return toSceneResponse(scene);
    }

    @Transactional
    public SceneResponse unpublishScene(UUID id) {
        Scene scene = sceneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scene not found with id: " + id));
        scene.setStatus(PublishStatus.UNPUBLISHED);
        scene.setPublishedUrl(null);
        scene = sceneRepository.save(scene);
        log.info("Scene unpublished: {}", id);
        return toSceneResponse(scene);
    }

    @Transactional
    public SceneResponse duplicateScene(UUID id, String newName) {
        Scene original = sceneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scene not found with id: " + id));

        Scene duplicate = Scene.builder()
                .tenantId(original.getTenantId())
                .name(newName != null ? newName : original.getName() + " (Copy)")
                .description(original.getDescription())
                .type(original.getType())
                .thumbnailUrl(original.getThumbnailUrl())
                .backgroundType(original.getBackgroundType())
                .backgroundAssetId(original.getBackgroundAssetId())
                .status(PublishStatus.DRAFT)
                .version(1)
                .settings(original.getSettings())
                .viewCount(0)
                .avgViewTimeSeconds(0.0)
                .build();
        duplicate = sceneRepository.save(duplicate);

        List<SceneNode> nodes = sceneNodeRepository.findBySceneIdOrderBySortOrderAsc(id);
        for (SceneNode originalNode : nodes) {
            SceneNode copiedNode = SceneNode.builder()
                    .sceneId(duplicate.getId())
                    .parentId(originalNode.getParentId())
                    .nodeType(originalNode.getNodeType())
                    .name(originalNode.getName())
                    .positionX(originalNode.getPositionX())
                    .positionY(originalNode.getPositionY())
                    .positionZ(originalNode.getPositionZ())
                    .rotationX(originalNode.getRotationX())
                    .rotationY(originalNode.getRotationY())
                    .rotationZ(originalNode.getRotationZ())
                    .scaleX(originalNode.getScaleX())
                    .scaleY(originalNode.getScaleY())
                    .scaleZ(originalNode.getScaleZ())
                    .content(originalNode.getContent())
                    .visible(originalNode.getVisible())
                    .interactive(originalNode.getInteractive())
                    .sortOrder(originalNode.getSortOrder())
                    .createdAt(LocalDateTime.now())
                    .isDeleted(false)
                    .build();
            sceneNodeRepository.save(copiedNode);
        }

        List<Hotspot> hotspots = hotspotRepository.findBySceneId(id);
        for (Hotspot originalHotspot : hotspots) {
            Hotspot copiedHotspot = Hotspot.builder()
                    .sceneId(duplicate.getId())
                    .nodeId(originalHotspot.getNodeId())
                    .hotspotType(originalHotspot.getHotspotType())
                    .latitude(originalHotspot.getLatitude())
                    .longitude(originalHotspot.getLongitude())
                    .title(originalHotspot.getTitle())
                    .description(originalHotspot.getDescription())
                    .iconUrl(originalHotspot.getIconUrl())
                    .actionType(originalHotspot.getActionType())
                    .actionPayload(originalHotspot.getActionPayload())
                    .style(originalHotspot.getStyle())
                    .animation(originalHotspot.getAnimation())
                    .createdAt(LocalDateTime.now())
                    .isDeleted(false)
                    .build();
            hotspotRepository.save(copiedHotspot);
        }

        log.info("Scene duplicated: {} -> {}", id, duplicate.getId());
        return toSceneResponse(duplicate);
    }

    @Transactional
    public SceneResponse incrementViewCount(UUID id) {
        Scene scene = sceneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scene not found with id: " + id));
        scene.setViewCount(scene.getViewCount() + 1);
        scene = sceneRepository.save(scene);
        return toSceneResponse(scene);
    }

    private SceneResponse toSceneResponse(Scene scene) {
        SceneResponse response = sceneMapper.toResponse(scene);
        response.setNodeCount(sceneNodeRepository.countBySceneId(scene.getId()));
        response.setHotspotCount(hotspotRepository.countBySceneId(scene.getId()));
        return response;
    }
}
