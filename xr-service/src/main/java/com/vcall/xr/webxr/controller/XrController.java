package com.vcall.xr.webxr.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.xr.webxr.domain.DeviceType;
import com.vcall.xr.webxr.domain.XrSession;
import com.vcall.xr.webxr.service.XrSessionService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/xr-sessions")
@RequiredArgsConstructor
public class XrController {

    private final XrSessionService xrSessionService;

    @PostMapping
    public ResponseEntity<ApiResponse<XrSession>> startSession(@Valid @RequestBody XrSession request) {
        XrSession session = xrSessionService.startSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Session started", session));
    }

    @PostMapping("/{id}/end")
    public ResponseEntity<ApiResponse<XrSession>> endSession(@PathVariable UUID id) {
        XrSession session = xrSessionService.endSession(id);
        return ResponseEntity.ok(ApiResponse.success("Session ended", session));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<XrSession>> getSession(@PathVariable UUID id) {
        XrSession session = xrSessionService.getSession(id);
        return ResponseEntity.ok(ApiResponse.success(session));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<XrSession>>> getSessionsByUser(
            @PathVariable UUID userId, Pageable pageable) {
        Page<XrSession> sessions = xrSessionService.getSessionsByUser(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @GetMapping("/scene/{sceneId}")
    public ResponseEntity<ApiResponse<Page<XrSession>>> getSessionsByScene(
            @PathVariable UUID sceneId, Pageable pageable) {
        Page<XrSession> sessions = xrSessionService.getSessionsByScene(sceneId, pageable);
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @GetMapping("/device/{deviceType}")
    public ResponseEntity<ApiResponse<Page<XrSession>>> getSessionsByDeviceType(
            @PathVariable DeviceType deviceType, Pageable pageable) {
        Page<XrSession> sessions = xrSessionService.getSessionsByDeviceType(deviceType, pageable);
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<Page<XrSession>>> getSessionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            Pageable pageable) {
        Page<XrSession> sessions = xrSessionService.getSessionsByDateRange(start, end, pageable);
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @PutMapping("/{id}/gaze-data")
    public ResponseEntity<ApiResponse<XrSession>> updateGazeData(
            @PathVariable UUID id, @RequestBody Map<String, String> body) {
        XrSession session = xrSessionService.updateGazeData(id, body.get("gazeData"));
        return ResponseEntity.ok(ApiResponse.success("Gaze data updated", session));
    }

    @PutMapping("/{id}/interactions")
    public ResponseEntity<ApiResponse<XrSession>> updateInteractions(
            @PathVariable UUID id, @RequestBody Map<String, String> body) {
        XrSession session = xrSessionService.updateInteractions(id, body.get("interactions"));
        return ResponseEntity.ok(ApiResponse.success("Interactions updated", session));
    }

    @PutMapping("/{id}/fps")
    public ResponseEntity<ApiResponse<XrSession>> updateFps(
            @PathVariable UUID id, @RequestBody Map<String, Double> body) {
        XrSession session = xrSessionService.updateFps(id, body.get("fpsAvg"));
        return ResponseEntity.ok(ApiResponse.success("FPS updated", session));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats(
            @RequestParam(defaultValue = "default") String tenantId) {
        Map<String, Object> stats = xrSessionService.getSessionStats(tenantId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
