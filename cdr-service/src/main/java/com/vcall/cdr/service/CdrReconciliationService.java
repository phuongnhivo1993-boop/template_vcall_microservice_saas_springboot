package com.vcall.cdr.service;

import com.vcall.cdr.entity.CdrRecord;
import com.vcall.cdr.repository.CdrRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CdrReconciliationService {

    private final CdrRecordRepository cdrRecordRepository;

    public ReconciliationReport reconcileWithBilling(LocalDate startDate, LocalDate endDate, UUID tenantId) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<CdrRecord> records = cdrRecordRepository.findByStartTimeBetween(start, end);

        if (tenantId != null) {
            records = records.stream()
                    .filter(r -> tenantId.equals(r.getTenantId()))
                    .collect(Collectors.toList());
        }

        ReconciliationReport report = new ReconciliationReport();
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setTenantId(tenantId);
        report.setTotalRecords(records.size());

        long answeredCount = records.stream().filter(r -> r.getStatus() == CdrRecord.Status.ANSWERED).count();
        long missedCount = records.stream().filter(r -> r.getStatus() != CdrRecord.Status.ANSWERED).count();

        report.setAnsweredCalls(answeredCount);
        report.setMissedCalls(missedCount);

        BigDecimal totalCost = records.stream()
                .filter(r -> r.getCost() != null)
                .map(CdrRecord::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalCost(totalCost);

        List<Discrepancy> discrepancies = detectDiscrepancies(records);
        report.setDiscrepancies(discrepancies);
        report.setDiscrepancyCount(discrepancies.size());

        return report;
    }

    public List<Discrepancy> detectDiscrepancies(List<CdrRecord> records) {
        List<Discrepancy> discrepancies = new ArrayList<>();

        for (CdrRecord record : records) {
            if (record.getStartTime() == null) {
                discrepancies.add(new Discrepancy(record.getCallId(), "Missing start time"));
            }
            if (record.getStatus() == CdrRecord.Status.ANSWERED && record.getDuration() == null) {
                discrepancies.add(new Discrepancy(record.getCallId(), "Answered call missing duration"));
            }
            if (record.getStatus() == CdrRecord.Status.ANSWERED && record.getAnswerTime() == null) {
                discrepancies.add(new Discrepancy(record.getCallId(), "Answered call missing answer time"));
            }
            if (record.getCost() == null || record.getCost().compareTo(BigDecimal.ZERO) < 0) {
                discrepancies.add(new Discrepancy(record.getCallId(), "Invalid or missing cost"));
            }
            if (record.getTenantId() == null) {
                discrepancies.add(new Discrepancy(record.getCallId(), "Missing tenant ID"));
            }
        }

        return discrepancies;
    }

    public ReconciliationReport generateReport(LocalDate startDate, LocalDate endDate, UUID tenantId) {
        return reconcileWithBilling(startDate, endDate, tenantId);
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReconciliationReport {
        private LocalDate startDate;
        private LocalDate endDate;
        private UUID tenantId;
        private long totalRecords;
        private long answeredCalls;
        private long missedCalls;
        private BigDecimal totalCost;
        private List<Discrepancy> discrepancies;
        private int discrepancyCount;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class Discrepancy {
        private String callId;
        private String description;
    }
}
