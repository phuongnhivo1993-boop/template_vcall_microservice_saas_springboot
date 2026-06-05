package com.vcall.xr.scene.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.xr.scene.domain.SceneNode;
import com.vcall.xr.scene.dto.SceneNodeRequest;
import com.vcall.xr.scene.dto.SceneNodeResponse;
import com.vcall.xr.scene.mapper.SceneNodeMapper;
import com.vcall.xr.scene.repository.SceneNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SceneGraphService {

    private final SceneNodeRepository sceneNodeRepository;
    private final SceneNodeMapper sceneNodeMapper;

    @Transactional
    public SceneNodeResponse addNode(UUID sceneId, SceneNodeRequest request) {
        SceneNode node = sceneNodeMapper.toEntity(request);
        node.setSceneId(sceneId);
        node.setCreatedAt(LocalDateTime.now());
        node.setIsDeleted(false);
        node = sceneNodeRepository.save(node);
        log.info("Node added to scene {}: {}", sceneId, node.getId());
        return sceneNodeMapper.toResponse(node);
    }

    @Transactional(readOnly = true)
    public SceneNodeResponse getNodeById(UUID nodeId) {
        SceneNode node = sceneNodeRepository.findById(nodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Scene node not found with id: " + nodeId));
        return sceneNodeMapper.toResponse(node);
    }

    @Transactional(readOnly = true)
    public List<SceneNodeResponse> getNodesByScene(UUID sceneId) {
        return sceneNodeRepository.findBySceneIdOrderBySortOrderAsc(sceneId).stream()
                .map(sceneNodeMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SceneNodeResponse> getChildNodes(UUID sceneId, UUID parentId) {
        return sceneNodeRepository.findBySceneIdAndParentIdOrderBySortOrderAsc(sceneId, parentId).stream()
                .map(sceneNodeMapper::toResponse)
                .toList();
    }

    @Transactional
    public SceneNodeResponse updateNode(UUID nodeId, SceneNodeRequest request) {
        SceneNode node = sceneNodeRepository.findById(nodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Scene node not found with id: " + nodeId));
        sceneNodeMapper.updateEntity(request, node);
        node.setUpdatedAt(LocalDateTime.now());
        node = sceneNodeRepository.save(node);
        log.info("Node updated: {}", nodeId);
        return sceneNodeMapper.toResponse(node);
    }

    @Transactional
    public void deleteNode(UUID nodeId) {
        SceneNode node = sceneNodeRepository.findById(nodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Scene node not found with id: " + nodeId));
        node.setIsDeleted(true);
        node.setUpdatedAt(LocalDateTime.now());
        sceneNodeRepository.save(node);
        log.info("Node deleted (soft): {}", nodeId);
    }

    @Transactional
    public void deleteAllNodesByScene(UUID sceneId) {
        List<SceneNode> nodes = sceneNodeRepository.findBySceneIdOrderBySortOrderAsc(sceneId);
        for (SceneNode node : nodes) {
            node.setIsDeleted(true);
            node.setUpdatedAt(LocalDateTime.now());
        }
        sceneNodeRepository.saveAll(nodes);
        log.info("All nodes deleted for scene: {}", sceneId);
    }

    @Transactional
    public List<SceneNodeResponse> reorderNodes(UUID sceneId, List<UUID> nodeOrder) {
        List<SceneNode> nodes = sceneNodeRepository.findBySceneIdOrderBySortOrderAsc(sceneId);
        for (int i = 0; i < nodeOrder.size(); i++) {
            UUID nodeId = nodeOrder.get(i);
            nodes.stream()
                    .filter(n -> n.getId().equals(nodeId))
                    .findFirst()
                    .ifPresent(n -> {
                        n.setSortOrder(i);
                        n.setUpdatedAt(LocalDateTime.now());
                    });
        }
        sceneNodeRepository.saveAll(nodes);
        log.info("Nodes reordered for scene: {}", sceneId);
        return getNodesByScene(sceneId);
    }
}
