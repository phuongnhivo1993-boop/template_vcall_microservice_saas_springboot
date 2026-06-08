package com.vcall.xr.twin.service;

import com.vcall.xr.twin.domain.DigitalTwin;
import com.vcall.xr.twin.repository.DigitalTwinRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DigitalTwinService {

    private final DigitalTwinRepository digitalTwinRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TWIN_SYNC_TOPIC = "xr.twin.sync";
    private static final String TWIN_IOT_TOPIC = "xr.twin.iot";

    @Transactional
    public DigitalTwin createDigitalTwin(DigitalTwin twin) {
        twin.setCreatedAt(LocalDateTime.now());
        twin.setUpdatedAt(LocalDateTime.now());
        twin.setIsDeleted(false);
        if (twin.getSyncIntervalSeconds() == null) {
            twin.setSyncIntervalSeconds(30);
        }
        DigitalTwin saved = digitalTwinRepository.save(twin);
        log.info("Created digital twin {} for tenant {}", saved.getId(), saved.getTenantId());
        kafkaTemplate.send(TWIN_SYNC_TOPIC, "TWIN_CREATED", saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public DigitalTwin getDigitalTwin(UUID id) {
        return digitalTwinRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Digital twin not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<DigitalTwin> getByTenant(UUID tenantId) {
        return digitalTwinRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public List<DigitalTwin> getByType(String type) {
        return digitalTwinRepository.findByType(type);
    }

    @Transactional(readOnly = true)
    public List<DigitalTwin> getBySceneId(UUID sceneId) {
        return digitalTwinRepository.findBySceneId(sceneId);
    }

    @Transactional
    public DigitalTwin updateDigitalTwin(UUID id, DigitalTwin update) {
        DigitalTwin twin = getDigitalTwin(id);
        twin.setName(update.getName());
        twin.setType(update.getType());
        twin.setBimAssetId(update.getBimAssetId());
        twin.setSceneId(update.getSceneId());
        twin.setIotEndpoints(update.getIotEndpoints());
        twin.setSyncIntervalSeconds(update.getSyncIntervalSeconds());
        twin.setFloors(update.getFloors());
        twin.setRooms(update.getRooms());
        twin.setUpdatedAt(LocalDateTime.now());
        DigitalTwin saved = digitalTwinRepository.save(twin);
        kafkaTemplate.send(TWIN_SYNC_TOPIC, "TWIN_UPDATED", saved);
        return saved;
    }

    @Transactional
    public void deleteDigitalTwin(UUID id) {
        DigitalTwin twin = getDigitalTwin(id);
        twin.setIsDeleted(true);
        twin.setUpdatedAt(LocalDateTime.now());
        digitalTwinRepository.save(twin);
        kafkaTemplate.send(TWIN_SYNC_TOPIC, "TWIN_DELETED", id.toString());
        log.info("Deleted digital twin {}", id);
    }

    @Transactional
    public DigitalTwin syncTwinData(UUID id, String syncPayload) {
        DigitalTwin twin = getDigitalTwin(id);
        twin.setUpdatedAt(LocalDateTime.now());
        DigitalTwin saved = digitalTwinRepository.save(twin);
        kafkaTemplate.send(TWIN_IOT_TOPIC, "TWIN_SYNC", Map.of("twinId", id, "payload", syncPayload));
        log.info("Synced data for digital twin {}", id);
        return saved;
    }

    @Transactional
    public void updateIoTEndpoints(UUID id, String endpoints) {
        DigitalTwin twin = getDigitalTwin(id);
        twin.setIotEndpoints(endpoints);
        twin.setUpdatedAt(LocalDateTime.now());
        digitalTwinRepository.save(twin);
        kafkaTemplate.send(TWIN_IOT_TOPIC, "IOT_ENDPOINTS_UPDATED", Map.of("twinId", id, "endpoints", endpoints));
        log.info("Updated IoT endpoints for digital twin {}", id);
    }
}
