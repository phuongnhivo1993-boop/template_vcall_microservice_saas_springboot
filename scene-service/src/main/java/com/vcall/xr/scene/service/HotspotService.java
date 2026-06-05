package com.vcall.xr.scene.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.xr.scene.domain.Hotspot;
import com.vcall.xr.scene.dto.HotspotRequest;
import com.vcall.xr.scene.dto.HotspotResponse;
import com.vcall.xr.scene.mapper.HotspotMapper;
import com.vcall.xr.scene.repository.HotspotRepository;
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
public class HotspotService {

    private final HotspotRepository hotspotRepository;
    private final HotspotMapper hotspotMapper;

    @Transactional
    public HotspotResponse createHotspot(UUID sceneId, HotspotRequest request) {
        Hotspot hotspot = hotspotMapper.toEntity(request);
        hotspot.setSceneId(sceneId);
        hotspot.setCreatedAt(LocalDateTime.now());
        hotspot.setIsDeleted(false);
        hotspot = hotspotRepository.save(hotspot);
        log.info("Hotspot created for scene {}: {}", sceneId, hotspot.getId());
        return hotspotMapper.toResponse(hotspot);
    }

    @Transactional(readOnly = true)
    public HotspotResponse getHotspotById(UUID hotspotId) {
        Hotspot hotspot = hotspotRepository.findById(hotspotId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotspot not found with id: " + hotspotId));
        return hotspotMapper.toResponse(hotspot);
    }

    @Transactional(readOnly = true)
    public List<HotspotResponse> getHotspotsByScene(UUID sceneId) {
        return hotspotRepository.findBySceneId(sceneId).stream()
                .map(hotspotMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HotspotResponse> getHotspotsByNode(UUID nodeId) {
        return hotspotRepository.findByNodeId(nodeId).stream()
                .map(hotspotMapper::toResponse)
                .toList();
    }

    @Transactional
    public HotspotResponse updateHotspot(UUID hotspotId, HotspotRequest request) {
        Hotspot hotspot = hotspotRepository.findById(hotspotId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotspot not found with id: " + hotspotId));
        hotspotMapper.updateEntity(request, hotspot);
        hotspot.setUpdatedAt(LocalDateTime.now());
        hotspot = hotspotRepository.save(hotspot);
        log.info("Hotspot updated: {}", hotspotId);
        return hotspotMapper.toResponse(hotspot);
    }

    @Transactional
    public void deleteHotspot(UUID hotspotId) {
        Hotspot hotspot = hotspotRepository.findById(hotspotId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotspot not found with id: " + hotspotId));
        hotspot.setIsDeleted(true);
        hotspot.setUpdatedAt(LocalDateTime.now());
        hotspotRepository.save(hotspot);
        log.info("Hotspot deleted (soft): {}", hotspotId);
    }
}
