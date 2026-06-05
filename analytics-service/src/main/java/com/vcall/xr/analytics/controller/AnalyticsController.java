package com.vcall.xr.analytics.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.xr.analytics.domain.AnalyticsEvent;
import com.vcall.xr.analytics.service.AnalyticsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping("/events")
    public ResponseEntity<ApiResponse<AnalyticsEvent>> trackEvent(@Valid @RequestBody AnalyticsEvent event) {
        AnalyticsEvent saved = analyticsService.trackEvent(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Event tracked", saved));
    }

    @PostMapping("/events/batch")
    public ResponseEntity<ApiResponse<String>> trackEventsBatch(@RequestBody java.util.List<AnalyticsEvent> events) {
        events.forEach(analyticsService::trackEvent);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Batch events tracked"));
    }

    @GetMapping("/events/session/{sessionId}")
    public ResponseEntity<ApiResponse<Page<AnalyticsEvent>>> getEventsBySession(
            @PathVariable UUID sessionId, Pageable pageable) {
        Page<AnalyticsEvent> events = analyticsService.getEventsBySession(sessionId, pageable);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/events/user/{userId}")
    public ResponseEntity<ApiResponse<Page<AnalyticsEvent>>> getEventsByUser(
            @PathVariable UUID userId, Pageable pageable) {
        Page<AnalyticsEvent> events = analyticsService.getEventsByUser(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/events/scene/{sceneId}")
    public ResponseEntity<ApiResponse<Page<AnalyticsEvent>>> getEventsByScene(
            @PathVariable UUID sceneId, Pageable pageable) {
        Page<AnalyticsEvent> events = analyticsService.getEventsByScene(sceneId, pageable);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/events/type/{eventType}")
    public ResponseEntity<ApiResponse<Page<AnalyticsEvent>>> getEventsByType(
            @PathVariable String eventType, Pageable pageable) {
        Page<AnalyticsEvent> events = analyticsService.getEventsByType(eventType, pageable);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/events/date-range")
    public ResponseEntity<ApiResponse<Page<AnalyticsEvent>>> getEventsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            Pageable pageable) {
        Page<AnalyticsEvent> events = analyticsService.getEventsByDateRange(start, end, pageable);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEventStats(
            @RequestParam(defaultValue = "default") String tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        Map<String, Object> stats = analyticsService.getEventStats(tenantId, start, end);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/scenes/top-views")
    public ResponseEntity<ApiResponse<java.util.List<Object[]>>> getTopScenesByViews(
            @RequestParam(defaultValue = "default") String tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "10") int limit) {
        java.util.List<Object[]> topScenes = analyticsService.getTopScenesByViews(tenantId, start, end, limit);
        return ResponseEntity.ok(ApiResponse.success(topScenes));
    }

    @GetMapping("/scenes/top-interactions")
    public ResponseEntity<ApiResponse<java.util.List<Object[]>>> getTopScenesByInteractions(
            @RequestParam(defaultValue = "default") String tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "10") int limit) {
        java.util.List<Object[]> topScenes = analyticsService.getTopScenesByInteractions(tenantId, start, end, limit);
        return ResponseEntity.ok(ApiResponse.success(topScenes));
    }

    @GetMapping("/gaze-heatmap/{sceneId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGazeHeatmap(
            @PathVariable UUID sceneId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        Map<String, Object> heatmap = analyticsService.getGazeHeatmapData(sceneId, start, end);
        return ResponseEntity.ok(ApiResponse.success(heatmap));
    }
}
