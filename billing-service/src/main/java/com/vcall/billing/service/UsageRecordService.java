package com.vcall.billing.service;

import com.vcall.billing.dto.UsageRecordRequest;
import com.vcall.billing.dto.UsageRecordResponse;
import com.vcall.billing.entity.UsageRecord;
import com.vcall.billing.repository.UsageRecordRepository;
import com.vcall.common.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsageRecordService {

    private final UsageRecordRepository usageRecordRepository;

    @Transactional
    public UsageRecordResponse recordUsage(UsageRecordRequest request) {
        if (request.getSource() != null && request.getSourceId() != null) {
            if (usageRecordRepository.findBySourceAndSourceId(request.getSource(), request.getSourceId()).isPresent()) {
                throw new DuplicateResourceException("Usage record already exists for source: " + request.getSource()
                        + " with id: " + request.getSourceId());
            }
        }
        UsageRecord record = new UsageRecord();
        record.setSubscriberId(request.getSubscriberId());
        record.setUsageType(request.getUsageType());
        record.setQuantity(request.getQuantity());
        record.setUnitPrice(request.getUnitPrice());
        record.setTotalCost(request.getUnitPrice() != null
                ? request.getQuantity().multiply(request.getUnitPrice()) : BigDecimal.ZERO);
        record.setRecordedAt(LocalDateTime.now());
        record.setSource(request.getSource());
        record.setSourceId(request.getSourceId());
        record = usageRecordRepository.save(record);
        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public List<UsageRecordResponse> getUsageBySubscriber(UUID subscriberId) {
        return usageRecordRepository.findAll().stream()
                .filter(r -> r.getSubscriberId().equals(subscriberId))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UsageRecordResponse> getUsageByTypeAndPeriod(UUID subscriberId, UsageRecord.UsageType usageType,
                                                              LocalDateTime startDate, LocalDateTime endDate) {
        return usageRecordRepository
                .findBySubscriberIdAndUsageTypeAndRecordedAtBetween(subscriberId, usageType, startDate, endDate)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UsageSummary aggregateUsage(UUID subscriberId, LocalDateTime startDate, LocalDateTime endDate) {
        List<UsageRecord> records = usageRecordRepository.findAll().stream()
                .filter(r -> r.getSubscriberId().equals(subscriberId)
                        && !r.getRecordedAt().isBefore(startDate)
                        && !r.getRecordedAt().isAfter(endDate))
                .collect(Collectors.toList());

        BigDecimal totalCost = records.stream()
                .map(r -> r.getTotalCost() != null ? r.getTotalCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalMinutes = records.stream()
                .filter(r -> r.getUsageType() == UsageRecord.UsageType.CALL_MINUTES)
                .map(UsageRecord::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRecordingMinutes = records.stream()
                .filter(r -> r.getUsageType() == UsageRecord.UsageType.RECORDING_MINUTES)
                .map(UsageRecord::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSms = records.stream()
                .filter(r -> r.getUsageType() == UsageRecord.UsageType.SMS_COUNT)
                .map(UsageRecord::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalEmails = records.stream()
                .filter(r -> r.getUsageType() == UsageRecord.UsageType.EMAIL_COUNT)
                .map(UsageRecord::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return UsageSummary.builder()
                .totalCost(totalCost)
                .totalMinutes(totalMinutes)
                .totalRecordingMinutes(totalRecordingMinutes)
                .totalSms(totalSms)
                .totalEmails(totalEmails)
                .build();
    }

    private UsageRecordResponse toResponse(UsageRecord record) {
        return UsageRecordResponse.builder()
                .id(record.getId())
                .usageType(record.getUsageType())
                .quantity(record.getQuantity())
                .unitPrice(record.getUnitPrice())
                .totalCost(record.getTotalCost())
                .recordedAt(record.getRecordedAt())
                .source(record.getSource())
                .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UsageSummary {
        private BigDecimal totalCost;
        private BigDecimal totalMinutes;
        private BigDecimal totalRecordingMinutes;
        private BigDecimal totalSms;
        private BigDecimal totalEmails;
    }
}
