package com.vcall.audit.service;

import com.vcall.audit.dto.AuditLogResponse;
import com.vcall.audit.dto.AuditSearchRequest;
import com.vcall.audit.entity.AuditLog;
import com.vcall.audit.repository.AuditLogRepository;
import com.vcall.common.exception.ResourceNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public AuditLogResponse createLog(AuditLog auditLog) {
        if (auditLog.getTimestamp() == null) {
            auditLog.setTimestamp(LocalDateTime.now());
        }
        AuditLog saved = auditLogRepository.save(auditLog);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AuditLogResponse getById(UUID id) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AuditLog not found with id: " + id));
        return toResponse(auditLog);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> searchLogs(AuditSearchRequest request) {
        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (request.getActorId() != null) {
                predicates.add(cb.equal(root.get("actorId"), request.getActorId()));
            }
            if (request.getAction() != null) {
                predicates.add(cb.equal(root.get("action"), AuditLog.Action.valueOf(request.getAction())));
            }
            if (request.getResource() != null) {
                predicates.add(cb.like(cb.lower(root.get("resource")), "%" + request.getResource().toLowerCase() + "%"));
            }
            if (request.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), request.getStartDate()));
            }
            if (request.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), request.getEndDate()));
            }
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), AuditLog.AuditStatus.valueOf(request.getStatus())));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by(Sort.Direction.DESC, "timestamp"));
        return auditLogRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getByActor(UUID actorId) {
        return auditLogRepository.findByActorId(actorId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getByActor(UUID actorId, Pageable pageable) {
        return auditLogRepository.findByActorId(actorId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getByResource(String resource, String resourceId) {
        return auditLogRepository.findByResourceAndResourceId(resource, resourceId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getByResource(String resource, String resourceId, Pageable pageable) {
        return auditLogRepository.findByResourceAndResourceId(resource, resourceId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByTimestampBetween(start, end).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deleteLog(UUID id) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AuditLog not found with id: " + id));
        auditLog.setIsDeleted(true);
        auditLogRepository.save(auditLog);
    }

    @Transactional
    public void cleanupLogs(LocalDateTime beforeDate) {
        List<AuditLog> oldLogs = auditLogRepository.findByTimestampBefore(beforeDate);
        for (AuditLog log : oldLogs) {
            log.setIsDeleted(true);
        }
        auditLogRepository.saveAll(oldLogs);
    }

    public List<AuditLogResponse> exportLogs(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null) startDate = LocalDateTime.now().minusMonths(1);
        if (endDate == null) endDate = LocalDateTime.now();
        return getByDateRange(startDate, endDate);
    }

    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalLogs", auditLogRepository.count());
        return stats;
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .timestamp(log.getTimestamp())
                .actorId(log.getActorId())
                .actorType(log.getActorType() != null ? log.getActorType().name() : null)
                .action(log.getAction() != null ? log.getAction().name() : null)
                .resource(log.getResource())
                .resourceId(log.getResourceId())
                .resourceType(log.getResourceType())
                .details(log.getDetails())
                .ipAddress(log.getIpAddress())
                .correlationId(log.getCorrelationId())
                .status(log.getStatus() != null ? log.getStatus().name() : null)
                .build();
    }
}
