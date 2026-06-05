package com.vcall.xr.webxr.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.xr.webxr.domain.DeviceType;
import com.vcall.xr.webxr.domain.XrSession;
import com.vcall.xr.webxr.repository.XrSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class XrSessionService {

    private final XrSessionRepository xrSessionRepository;

    @Transactional
    public XrSession startSession(XrSession request) {
        request.setStartedAt(LocalDateTime.now());
        return xrSessionRepository.save(request);
    }

    @Transactional
    public XrSession endSession(UUID sessionId) {
        XrSession session = xrSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("XR Session not found: " + sessionId));
        session.setEndedAt(LocalDateTime.now());
        if (session.getStartedAt() != null) {
            session.setDurationSeconds(
                java.time.Duration.between(session.getStartedAt(), session.getEndedAt()).getSeconds()
            );
        }
        return xrSessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public XrSession getSession(UUID sessionId) {
        return xrSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("XR Session not found: " + sessionId));
    }

    @Transactional(readOnly = true)
    public Page<XrSession> getSessionsByUser(UUID userId, Pageable pageable) {
        return xrSessionRepository.findByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<XrSession> getSessionsByScene(UUID sceneId, Pageable pageable) {
        return xrSessionRepository.findBySceneId(sceneId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<XrSession> getSessionsByDeviceType(DeviceType deviceType, Pageable pageable) {
        return xrSessionRepository.findByDeviceType(deviceType, pageable);
    }

    @Transactional(readOnly = true)
    public Page<XrSession> getSessionsByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return xrSessionRepository.findByStartedAtBetween(start, end, pageable);
    }

    @Transactional
    public XrSession updateGazeData(UUID sessionId, String gazeData) {
        XrSession session = xrSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("XR Session not found: " + sessionId));
        session.setGazeData(gazeData);
        return xrSessionRepository.save(session);
    }

    @Transactional
    public XrSession updateInteractions(UUID sessionId, String interactions) {
        XrSession session = xrSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("XR Session not found: " + sessionId));
        session.setInteractions(interactions);
        return xrSessionRepository.save(session);
    }

    @Transactional
    public XrSession updateFps(UUID sessionId, Double fpsAvg) {
        XrSession session = xrSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("XR Session not found: " + sessionId));
        session.setFpsAvg(fpsAvg);
        return xrSessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSessionStats(String tenantId) {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        stats.put("activeSessions", xrSessionRepository.countActiveSessions());
        stats.put("totalSessionsToday", xrSessionRepository.countByTenantIdAndDateRange(
                tenantId, now.toLocalDate().atStartOfDay(), now));
        stats.put("averageFps", xrSessionRepository.averageFpsByTenantId(tenantId));
        stats.put("averageLoadTimeMs", xrSessionRepository.averageLoadTimeByTenantId(tenantId));
        stats.put("deviceTypeDistribution", xrSessionRepository.countByDeviceTypeGrouped(tenantId));
        return stats;
    }
}
