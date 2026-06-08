package com.vcall.automation.service;

import com.vcall.automation.dto.ExecutionLogResponse;
import com.vcall.automation.entity.ExecutionLog;
import com.vcall.automation.repository.ExecutionLogRepository;
import com.vcall.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExecutionLogService {

    private final ExecutionLogRepository executionLogRepository;

    @Transactional(readOnly = true)
    public List<ExecutionLogResponse> getLogs(Long ruleId) {
        return executionLogRepository.findByRuleId(ruleId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExecutionLogResponse getLog(Long id) {
        ExecutionLog log = executionLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Execution log not found with id: " + id));
        return toResponse(log);
    }

    private ExecutionLogResponse toResponse(ExecutionLog log) {
        return ExecutionLogResponse.builder()
                .id(log.getId())
                .ruleId(log.getRuleId())
                .triggeredBy(log.getTriggeredBy())
                .status(log.getStatus())
                .errorMessage(log.getErrorMessage())
                .executedAt(log.getExecutedAt())
                .build();
    }
}
