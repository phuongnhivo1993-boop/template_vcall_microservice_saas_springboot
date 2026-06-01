package com.vcall.audit.service;

import com.vcall.audit.dto.FraudAlertRequest;
import com.vcall.audit.dto.FraudAlertResponse;
import com.vcall.audit.dto.FraudAlertStatusRequest;
import com.vcall.audit.entity.FraudAlert;
import com.vcall.audit.entity.SecurityLog;
import com.vcall.audit.repository.FraudAlertRepository;
import com.vcall.audit.repository.SecurityLogRepository;
import com.vcall.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private final FraudAlertRepository fraudAlertRepository;
    private final SecurityLogRepository securityLogRepository;

    @Transactional
    public FraudAlertResponse detectFraud(String eventPayload) {
        FraudAlert.AlertType alertType = analyzePatterns(eventPayload);
        if (alertType == null) {
            return null;
        }
        FraudAlert alert = FraudAlert.builder()
                .detectedAt(LocalDateTime.now())
                .alertType(alertType)
                .severity(determineSeverity(alertType))
                .description("Automatically detected fraud pattern: " + alertType)
                .evidence(eventPayload)
                .status(FraudAlert.AlertStatus.OPEN)
                .build();
        FraudAlert saved = fraudAlertRepository.save(alert);
        return toResponse(saved);
    }

    private FraudAlert.AlertType analyzePatterns(String payload) {
        return null;
    }

    private FraudAlert.Severity determineSeverity(FraudAlert.AlertType alertType) {
        return switch (alertType) {
            case ACCOUNT_TAKEOVER -> FraudAlert.Severity.CRITICAL;
            case RAPID_FIRE_CALLS, RATE_MANIPULATION -> FraudAlert.Severity.HIGH;
            case MULTIPLE_LOGIN_FAILURES, UNUSUAL_LOCATION -> FraudAlert.Severity.MEDIUM;
            case UNUSUAL_HOURS, CALL_PADDING -> FraudAlert.Severity.LOW;
        };
    }

    @Transactional
    public FraudAlertResponse createAlert(FraudAlertRequest request) {
        FraudAlert alert = FraudAlert.builder()
                .detectedAt(LocalDateTime.now())
                .alertType(FraudAlert.AlertType.valueOf(request.getAlertType()))
                .severity(FraudAlert.Severity.valueOf(request.getSeverity()))
                .actorId(request.getActorId())
                .description(request.getDescription())
                .evidence(request.getEvidence())
                .status(FraudAlert.AlertStatus.OPEN)
                .build();
        FraudAlert saved = fraudAlertRepository.save(alert);
        return toResponse(saved);
    }

    @Transactional
    public FraudAlertResponse updateAlertStatus(UUID id, FraudAlertStatusRequest request) {
        FraudAlert alert = fraudAlertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FraudAlert not found with id: " + id));
        alert.setStatus(FraudAlert.AlertStatus.valueOf(request.getStatus()));
        if (request.getResolution() != null) {
            alert.setResolution(request.getResolution());
        }
        if (request.getStatus().equals(FraudAlert.AlertStatus.RESOLVED.name())
                || request.getStatus().equals(FraudAlert.AlertStatus.FALSE_POSITIVE.name())) {
            alert.setResolvedAt(LocalDateTime.now());
            alert.setResolvedBy("system");
        }
        FraudAlert saved = fraudAlertRepository.save(alert);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<FraudAlertResponse> getAlerts(FraudAlert.AlertStatus status, FraudAlert.Severity severity) {
        if (status != null) {
            return fraudAlertRepository.findByStatus(status).stream()
                    .map(this::toResponse)
                    .toList();
        }
        if (severity != null) {
            return fraudAlertRepository.findBySeverity(severity).stream()
                    .map(this::toResponse)
                    .toList();
        }
        return fraudAlertRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FraudAlertResponse getAlertById(UUID id) {
        FraudAlert alert = fraudAlertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FraudAlert not found with id: " + id));
        return toResponse(alert);
    }

    @Transactional
    public FraudAlertResponse investigateAlert(UUID id) {
        FraudAlert alert = fraudAlertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FraudAlert not found with id: " + id));
        alert.setStatus(FraudAlert.AlertStatus.INVESTIGATING);
        FraudAlert saved = fraudAlertRepository.save(alert);
        return toResponse(saved);
    }

    public FraudAlertResponse checkMultipleLoginFailures(UUID actorId, int thresholdMinutes, int maxAttempts) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(thresholdMinutes);
        List<SecurityLog> recentFailures = securityLogRepository.findByTimestampBetween(since, LocalDateTime.now())
                .stream()
                .filter(log -> log.getEventType() == SecurityLog.EventType.LOGIN_FAILED)
                .filter(log -> actorId == null || log.getActorId().equals(actorId))
                .toList();
        if (recentFailures.size() >= maxAttempts) {
            FraudAlert alert = FraudAlert.builder()
                    .detectedAt(LocalDateTime.now())
                    .alertType(FraudAlert.AlertType.MULTIPLE_LOGIN_FAILURES)
                    .severity(FraudAlert.Severity.MEDIUM)
                    .actorId(actorId)
                    .description("Multiple login failures detected: " + recentFailures.size() + " attempts in last "
                            + thresholdMinutes + " minutes")
                    .evidence("{\"failureCount\":" + recentFailures.size()
                            + ",\"thresholdMinutes\":" + thresholdMinutes
                            + ",\"maxAttempts\":" + maxAttempts + "}")
                    .status(FraudAlert.AlertStatus.OPEN)
                    .build();
            FraudAlert saved = fraudAlertRepository.save(alert);
            return toResponse(saved);
        }
        return null;
    }

    public FraudAlertResponse checkUnusualLocation(UUID actorId, String currentLocation, String lastKnownLocation) {
        if (currentLocation != null && lastKnownLocation != null
                && !currentLocation.equalsIgnoreCase(lastKnownLocation)) {
            FraudAlert alert = FraudAlert.builder()
                    .detectedAt(LocalDateTime.now())
                    .alertType(FraudAlert.AlertType.UNUSUAL_LOCATION)
                    .severity(FraudAlert.Severity.MEDIUM)
                    .actorId(actorId)
                    .description("Unusual login location detected. Last known: "
                            + lastKnownLocation + ", Current: " + currentLocation)
                    .evidence("{\"currentLocation\":\"" + currentLocation
                            + "\",\"lastKnownLocation\":\"" + lastKnownLocation + "\"}")
                    .status(FraudAlert.AlertStatus.OPEN)
                    .build();
            FraudAlert saved = fraudAlertRepository.save(alert);
            return toResponse(saved);
        }
        return null;
    }

    public FraudAlertResponse checkRapidFireCalls(UUID actorId, int thresholdSeconds, int maxCalls) {
        return null;
    }

    public FraudAlertResponse checkCallPadding(UUID actorId, int callDurationSeconds, int billedDurationSeconds) {
        if (billedDurationSeconds > callDurationSeconds * 1.2) {
            FraudAlert alert = FraudAlert.builder()
                    .detectedAt(LocalDateTime.now())
                    .alertType(FraudAlert.AlertType.CALL_PADDING)
                    .severity(FraudAlert.Severity.LOW)
                    .actorId(actorId)
                    .description("Call padding detected: call duration "
                            + callDurationSeconds + "s vs billed " + billedDurationSeconds + "s")
                    .evidence("{\"callDuration\":" + callDurationSeconds
                            + ",\"billedDuration\":" + billedDurationSeconds + "}")
                    .status(FraudAlert.AlertStatus.OPEN)
                    .build();
            FraudAlert saved = fraudAlertRepository.save(alert);
            return toResponse(saved);
        }
        return null;
    }

    private FraudAlertResponse toResponse(FraudAlert alert) {
        return FraudAlertResponse.builder()
                .id(alert.getId())
                .detectedAt(alert.getDetectedAt())
                .alertType(alert.getAlertType() != null ? alert.getAlertType().name() : null)
                .severity(alert.getSeverity() != null ? alert.getSeverity().name() : null)
                .actorId(alert.getActorId())
                .description(alert.getDescription())
                .status(alert.getStatus() != null ? alert.getStatus().name() : null)
                .resolvedBy(alert.getResolvedBy())
                .resolvedAt(alert.getResolvedAt())
                .build();
    }
}
