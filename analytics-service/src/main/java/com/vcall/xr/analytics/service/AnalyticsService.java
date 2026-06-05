package com.vcall.xr.analytics.service;

import com.vcall.xr.analytics.domain.AnalyticsEvent;
import com.vcall.xr.analytics.repository.AnalyticsEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
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
public class AnalyticsService {

    private final AnalyticsEventRepository analyticsEventRepository;

    @Transactional
    public AnalyticsEvent trackEvent(AnalyticsEvent event) {
        if (event.getTimestamp() == null) {
            event.setTimestamp(LocalDateTime.now());
        }
        log.debug("Tracking event: {} for tenant: {}", event.getEventType(), event.getTenantId());
        return analyticsEventRepository.save(event);
    }

    @KafkaListener(topics = "xr-analytics-events", groupId = "analytics-service")
    public void consumeAnalyticsEvent(String message, Acknowledgment acknowledgment) {
        log.debug("Received analytics event: {}", message);
        try {
            // Parse and track the event from Kafka message
            AnalyticsEvent event = new AnalyticsEvent();
            event.setEventType("KAFKA_EVENT");
            event.setEventData(message);
            event.setTimestamp(LocalDateTime.now());
            analyticsEventRepository.save(event);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing analytics event: {}", e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public Page<AnalyticsEvent> getEventsBySession(UUID sessionId, Pageable pageable) {
        return analyticsEventRepository.findBySessionId(sessionId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AnalyticsEvent> getEventsByUser(UUID userId, Pageable pageable) {
        return analyticsEventRepository.findByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AnalyticsEvent> getEventsByScene(UUID sceneId, Pageable pageable) {
        return analyticsEventRepository.findBySceneId(sceneId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AnalyticsEvent> getEventsByType(String eventType, Pageable pageable) {
        return analyticsEventRepository.findByEventType(eventType, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AnalyticsEvent> getEventsByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return analyticsEventRepository.findByTimestampBetween(start, end, pageable);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getEventStats(String tenantId, LocalDateTime start, LocalDateTime end) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEvents", analyticsEventRepository.countByTenantIdAndDateRange(tenantId, start, end));
        stats.put("eventTypeDistribution", analyticsEventRepository.countByEventTypeGrouped(tenantId, start, end));
        stats.put("deviceTypeDistribution", analyticsEventRepository.countByDeviceTypeGrouped(tenantId, start, end));
        stats.put("sessionActivityCounts", analyticsEventRepository.sessionActivityCounts(tenantId, start, end));
        return stats;
    }

    @Transactional(readOnly = true)
    public List<Object[]> getTopScenesByViews(String tenantId, LocalDateTime start, LocalDateTime end, int limit) {
        return analyticsEventRepository.topScenesByEventType(
                tenantId, "OBJECT_VIEW", start, end,
                org.springframework.data.domain.PageRequest.of(0, limit)
        );
    }

    @Transactional(readOnly = true)
    public List<Object[]> getTopScenesByInteractions(String tenantId, LocalDateTime start, LocalDateTime end, int limit) {
        return analyticsEventRepository.topScenesByEventType(
                tenantId, "INTERACTION_CLICK", start, end,
                org.springframework.data.domain.PageRequest.of(0, limit)
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getGazeHeatmapData(UUID sceneId, LocalDateTime start, LocalDateTime end) {
        Map<String, Object> heatmap = new HashMap<>();
        List<AnalyticsEvent> gazeEvents = analyticsEventRepository.findBySceneId(sceneId,
                org.springframework.data.domain.PageRequest.of(0, 10000)).getContent();

        List<Map<String, Object>> gazePoints = gazeEvents.stream()
                .filter(e -> e.getEventType().startsWith("GAZE_"))
                .map(e -> {
                    Map<String, Object> point = new HashMap<>();
                    point.put("eventType", e.getEventType());
                    point.put("timestamp", e.getTimestamp());
                    point.put("eventData", e.getEventData());
                    return point;
                })
                .toList();

        heatmap.put("sceneId", sceneId);
        heatmap.put("gazePoints", gazePoints);
        heatmap.put("totalGazeEvents", gazePoints.size());
        return heatmap;
    }
}
