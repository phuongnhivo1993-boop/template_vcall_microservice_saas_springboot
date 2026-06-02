package com.vcall.reporting.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.reporting.dto.ReportDefinitionRequest;
import com.vcall.reporting.dto.ReportDefinitionResponse;
import com.vcall.reporting.entity.ReportDefinition;
import com.vcall.reporting.repository.ReportDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportDefinitionService {

    private final ReportDefinitionRepository reportDefinitionRepository;
    private final ReportExecutionService reportExecutionService;
    private final ObjectMapper objectMapper;

    @Transactional
    public ReportDefinitionResponse createReport(ReportDefinitionRequest request) {
        ReportDefinition report = new ReportDefinition();
        report.setName(request.getName());
        report.setDescription(request.getDescription());
        report.setReportType(ReportDefinition.ReportType.valueOf(request.getReportType()));
        report.setParameters(request.getParameters());
        report.setSchedule(request.getSchedule());
        report.setRecipients(request.getRecipients());
        report.setActive(request.isActive());
        report = reportDefinitionRepository.save(report);
        return toResponse(report);
    }

    @Transactional
    public ReportDefinitionResponse updateReport(Long id, ReportDefinitionRequest request) {
        ReportDefinition report = reportDefinitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report definition not found with id: " + id));
        report.setName(request.getName());
        report.setDescription(request.getDescription());
        report.setReportType(ReportDefinition.ReportType.valueOf(request.getReportType()));
        report.setParameters(request.getParameters());
        report.setSchedule(request.getSchedule());
        report.setRecipients(request.getRecipients());
        report.setActive(request.isActive());
        report = reportDefinitionRepository.save(report);
        return toResponse(report);
    }

    @Transactional(readOnly = true)
    public ReportDefinitionResponse getReport(Long id) {
        ReportDefinition report = reportDefinitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report definition not found with id: " + id));
        return toResponse(report);
    }

    @Transactional(readOnly = true)
    public Page<ReportDefinitionResponse> getAllReports(Pageable pageable) {
        return reportDefinitionRepository.findByIsActiveTrue(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ReportDefinitionResponse> getReportsByType(String reportType, Pageable pageable) {
        return reportDefinitionRepository.findByReportType(ReportDefinition.ReportType.valueOf(reportType.toUpperCase()), pageable)
                .map(this::toResponse);
    }

    @Transactional
    public void deleteReport(Long id) {
        ReportDefinition report = reportDefinitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report definition not found with id: " + id));
        report.setIsDeleted(true);
        reportDefinitionRepository.save(report);
    }

    @Transactional
    public void executeReport(Long id) {
        ReportDefinition report = reportDefinitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report definition not found with id: " + id));
        reportExecutionService.runReport(report);
    }

    private ReportDefinitionResponse toResponse(ReportDefinition report) {
        return ReportDefinitionResponse.builder()
                .id(report.getId())
                .name(report.getName())
                .description(report.getDescription())
                .reportType(report.getReportType().name())
                .schedule(report.getSchedule())
                .isActive(report.isActive())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
