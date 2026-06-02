package com.vcall.audit.service;

import com.vcall.audit.dto.SecurityLogResponse;
import com.vcall.audit.entity.SecurityLog;
import com.vcall.audit.repository.SecurityLogRepository;
import com.vcall.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SecurityLogService {

    private final SecurityLogRepository securityLogRepository;

    @Transactional
    public SecurityLogResponse createLog(SecurityLog securityLog) {
        if (securityLog.getTimestamp() == null) {
            securityLog.setTimestamp(LocalDateTime.now());
        }
        if (securityLog.getIsSuspicious() == null) {
            securityLog.setIsSuspicious(false);
        }
        SecurityLog saved = securityLogRepository.save(securityLog);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public SecurityLogResponse getById(UUID id) {
        SecurityLog log = securityLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SecurityLog not found with id: " + id));
        return toResponse(log);
    }

    @Transactional(readOnly = true)
    public Page<SecurityLogResponse> getAllLogs(Pageable pageable) {
        return securityLogRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SecurityLogResponse> getByEventType(SecurityLog.EventType eventType, Pageable pageable) {
        return securityLogRepository.findByEventType(eventType, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SecurityLogResponse> getByRiskLevel(SecurityLog.RiskLevel riskLevel, Pageable pageable) {
        return securityLogRepository.findByRiskLevel(riskLevel, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<SecurityLogResponse> detectSuspiciousActivity() {
        return securityLogRepository.findByIsSuspiciousTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SecurityLogResponse> getLoginHistory(UUID actorId) {
        return securityLogRepository.findByActorId(actorId).stream()
                .filter(log -> log.getEventType() == SecurityLog.EventType.LOGIN_SUCCESS
                        || log.getEventType() == SecurityLog.EventType.LOGIN_FAILED
                        || log.getEventType() == SecurityLog.EventType.LOGOUT)
                .map(this::toResponse)
                .toList();
    }

    private SecurityLogResponse toResponse(SecurityLog log) {
        return SecurityLogResponse.builder()
                .id(log.getId())
                .timestamp(log.getTimestamp())
                .eventType(log.getEventType() != null ? log.getEventType().name() : null)
                .actorId(log.getActorId())
                .username(log.getUsername())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .details(log.getDetails())
                .riskLevel(log.getRiskLevel() != null ? log.getRiskLevel().name() : null)
                .isSuspicious(log.getIsSuspicious())
                .build();
    }
}
