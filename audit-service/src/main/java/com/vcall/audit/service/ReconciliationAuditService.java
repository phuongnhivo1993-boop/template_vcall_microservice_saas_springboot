package com.vcall.audit.service;

import com.vcall.audit.dto.ReconciliationAuditResponse;
import com.vcall.audit.entity.ReconciliationAudit;
import com.vcall.audit.repository.ReconciliationAuditRepository;
import com.vcall.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReconciliationAuditService {

    private final ReconciliationAuditRepository reconciliationAuditRepository;

    @Transactional
    public ReconciliationAuditResponse createAudit(ReconciliationAudit audit) {
        if (audit.getReconciliationDate() == null) {
            audit.setReconciliationDate(LocalDateTime.now());
        }
        if (audit.getStatus() == null) {
            audit.setStatus(ReconciliationAudit.ReconciliationStatus.PENDING);
        }
        ReconciliationAudit saved = reconciliationAuditRepository.save(audit);
        return toResponse(saved);
    }

    @Transactional
    public ReconciliationAuditResponse runReconciliation(ReconciliationAudit.ReconciliationType type) {
        ReconciliationAudit audit = ReconciliationAudit.builder()
                .reconciliationDate(LocalDateTime.now())
                .type(type)
                .totalRecords(0)
                .matchedCount(0)
                .unmatchedCount(0)
                .discrepancyCount(0)
                .status(ReconciliationAudit.ReconciliationStatus.PENDING)
                .build();
        audit = reconciliationAuditRepository.save(audit);

        try {
            audit.setStatus(ReconciliationAudit.ReconciliationStatus.COMPLETED);
            audit = reconciliationAuditRepository.save(audit);
        } catch (Exception e) {
            audit.setStatus(ReconciliationAudit.ReconciliationStatus.FAILED);
            audit.setReportData("{\"error\":\"" + e.getMessage() + "\"}");
            audit = reconciliationAuditRepository.save(audit);
        }

        return toResponse(audit);
    }

    @Transactional(readOnly = true)
    public ReconciliationAuditResponse getById(UUID id) {
        ReconciliationAudit audit = reconciliationAuditRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReconciliationAudit not found with id: " + id));
        return toResponse(audit);
    }

    @Transactional(readOnly = true)
    public List<ReconciliationAuditResponse> getDiscrepancies(ReconciliationAudit.ReconciliationType type,
                                                                ReconciliationAudit.ReconciliationStatus status) {
        if (type != null && status != null) {
            return reconciliationAuditRepository.findByTypeAndStatus(type, status).stream()
                    .map(this::toResponse)
                    .toList();
        }
        return reconciliationAuditRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReconciliationAuditResponse generateReport(UUID id) {
        ReconciliationAudit audit = reconciliationAuditRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReconciliationAudit not found with id: " + id));
        if (audit.getReportData() == null) {
            audit.setReportData("{\"totalRecords\":" + audit.getTotalRecords()
                    + ",\"matchedCount\":" + audit.getMatchedCount()
                    + ",\"unmatchedCount\":" + audit.getUnmatchedCount()
                    + ",\"discrepancyCount\":" + audit.getDiscrepancyCount()
                    + ",\"type\":\"" + audit.getType() + "\"}");
        }
        return toResponse(audit);
    }

    private ReconciliationAuditResponse toResponse(ReconciliationAudit audit) {
        return ReconciliationAuditResponse.builder()
                .id(audit.getId())
                .reconciliationDate(audit.getReconciliationDate())
                .type(audit.getType() != null ? audit.getType().name() : null)
                .totalRecords(audit.getTotalRecords())
                .matchedCount(audit.getMatchedCount())
                .unmatchedCount(audit.getUnmatchedCount())
                .discrepancyCount(audit.getDiscrepancyCount())
                .status(audit.getStatus() != null ? audit.getStatus().name() : null)
                .build();
    }
}
