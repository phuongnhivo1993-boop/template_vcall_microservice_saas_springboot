package com.vcall.cdr.service;

import com.vcall.cdr.dto.CdrRecordResponse;
import com.vcall.cdr.dto.CdrSearchRequest;
import com.vcall.cdr.entity.CdrRecord;
import com.vcall.cdr.kafka.CdrEventPublisher;
import com.vcall.cdr.repository.CdrRecordRepository;
import com.vcall.common.dto.PagedResponse;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CdrService {

    private final CdrRecordRepository cdrRecordRepository;
    private final CdrEventPublisher eventPublisher;

    @Transactional
    public CdrRecordResponse createCdr(CdrRecord record) {
        record = cdrRecordRepository.save(record);
        eventPublisher.publishCdrGenerated(record);
        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public CdrRecordResponse getById(UUID id) {
        CdrRecord record = cdrRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CDR not found with id: " + id));
        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public CdrRecordResponse getByCallId(String callId) {
        CdrRecord record = cdrRecordRepository.findByCallId(callId)
                .orElseThrow(() -> new ResourceNotFoundException("CDR not found with callId: " + callId));
        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public PagedResponse<CdrRecordResponse> searchCdr(CdrSearchRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by(Sort.Direction.DESC, "startTime"));

        Specification<CdrRecord> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getCallerNumber() != null && !request.getCallerNumber().isEmpty()) {
                predicates.add(cb.like(root.get("callerNumber"), "%" + request.getCallerNumber() + "%"));
            }
            if (request.getCalleeNumber() != null && !request.getCalleeNumber().isEmpty()) {
                predicates.add(cb.like(root.get("calleeNumber"), "%" + request.getCalleeNumber() + "%"));
            }
            if (request.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), request.getStartDate()));
            }
            if (request.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startTime"), request.getEndDate()));
            }
            if (request.getStatus() != null && !request.getStatus().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), CdrRecord.Status.valueOf(request.getStatus().toUpperCase())));
            }
            if (request.getAgentId() != null) {
                predicates.add(cb.equal(root.get("agentId"), request.getAgentId()));
            }
            if (request.getDirection() != null && !request.getDirection().isEmpty()) {
                predicates.add(cb.equal(root.get("direction"), CdrRecord.Direction.valueOf(request.getDirection().toUpperCase())));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<CdrRecord> page = cdrRecordRepository.findAll(spec, pageable);
        List<CdrRecordResponse> content = page.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.<CdrRecordResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<CdrRecordResponse> getByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return cdrRecordRepository.findByStartTimeBetween(start, end, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CdrRecordResponse> getByTenant(UUID tenantId, Pageable pageable) {
        return cdrRecordRepository.findByTenantId(tenantId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CdrRecordResponse> getByAgentId(UUID agentId, Pageable pageable) {
        return cdrRecordRepository.findByAgentId(agentId, pageable).map(this::toResponse);
    }

    private CdrRecordResponse toResponse(CdrRecord record) {
        return CdrRecordResponse.builder()
                .id(record.getId())
                .callId(record.getCallId())
                .callerNumber(record.getCallerNumber())
                .calleeNumber(record.getCalleeNumber())
                .direction(record.getDirection().name())
                .startTime(record.getStartTime())
                .answerTime(record.getAnswerTime())
                .endTime(record.getEndTime())
                .duration(record.getDuration())
                .status(record.getStatus().name())
                .hangupCause(record.getHangupCause())
                .agentId(record.getAgentId())
                .queueId(record.getQueueId())
                .recordingId(record.getRecordingId())
                .cost(record.getCost())
                .rate(record.getRate())
                .build();
    }
}
