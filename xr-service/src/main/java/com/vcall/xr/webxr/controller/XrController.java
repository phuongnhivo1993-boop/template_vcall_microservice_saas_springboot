package com.vcall.xr.webxr.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.xr.webxr.domain.DeviceType;
import com.vcall.xr.webxr.dto.XrSessionMapper;
import com.vcall.xr.webxr.dto.XrSessionRequest;
import com.vcall.xr.webxr.dto.XrSessionResponse;
import com.vcall.xr.webxr.service.XrSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<XrSessionResponse>> startSession(@Valid @RequestBody XrSessionRequest request) {
        XrSessionResponse session = XrSessionMapper.toResponse(
                xrSessionService.startSession(XrSessionMapper.toEntity(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Session started", session));
    }

    @PostMapping("/{id}/end")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<XrSessionResponse>> endSession(@PathVariable UUID id) {
        XrSessionResponse session = XrSessionMapper.toResponse(xrSessionService.endSession(id));
        return ResponseEntity.ok(ApiResponse.success("Session ended", session));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<XrSessionResponse>> getSession(@PathVariable UUID id) {
        XrSessionResponse session = XrSessionMapper.toResponse(xrSessionService.getSession(id));
        return ResponseEntity.ok(ApiResponse.success(session));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<XrSessionResponse>>> getSessionsByUser(
            @PathVariable UUID userId, Pageable pageable) {
        Page<XrSessionResponse> sessions = xrSessionService.getSessionsByUser(userId, pageable)
                .map(XrSessionMapper::toResponse);
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @GetMapping("/scene/{sceneId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<XrSessionResponse>>> getSessionsByScene(
            @PathVariable UUID sceneId, Pageable pageable) {
        Page<XrSessionResponse> sessions = xrSessionService.getSessionsByScene(sceneId, pageable)
                .map(XrSessionMapper::toResponse);
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @GetMapping("/device/{deviceType}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<XrSessionResponse>>> getSessionsByDeviceType(
            @PathVariable DeviceType deviceType, Pageable pageable) {
        Page<XrSessionResponse> sessions = xrSessionService.getSessionsByDeviceType(deviceType, pageable)
                .map(XrSessionMapper::toResponse);
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @GetMapping("/date-range")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<XrSessionResponse>>> getSessionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            Pageable pageable) {
        Page<XrSessionResponse> sessions = xrSessionService.getSessionsByDateRange(start, end, pageable)
                .map(XrSessionMapper::toResponse);
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @PutMapping("/{id}/gaze-data")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<XrSessionResponse>> updateGazeData(
            @PathVariable UUID id, @RequestBody Map<String, String> body) {
        XrSessionResponse session = XrSessionMapper.toResponse(
                xrSessionService.updateGazeData(id, body.get("gazeData")));
        return ResponseEntity.ok(ApiResponse.success("Gaze data updated", session));
    }

    @PutMapping("/{id}/interactions")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<XrSessionResponse>> updateInteractions(
            @PathVariable UUID id, @RequestBody Map<String, String> body) {
        XrSessionResponse session = XrSessionMapper.toResponse(
                xrSessionService.updateInteractions(id, body.get("interactions")));
        return ResponseEntity.ok(ApiResponse.success("Interactions updated", session));
    }

    @PutMapping("/{id}/fps")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<XrSessionResponse>> updateFps(
            @PathVariable UUID id, @RequestBody Map<String, Double> body) {
        XrSessionResponse session = XrSessionMapper.toResponse(
                xrSessionService.updateFps(id, body.get("fpsAvg")));
        return ResponseEntity.ok(ApiResponse.success("FPS updated", session));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats(
            @RequestParam(defaultValue = "default") String tenantId) {
        Map<String, Object> stats = xrSessionService.getSessionStats(tenantId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
