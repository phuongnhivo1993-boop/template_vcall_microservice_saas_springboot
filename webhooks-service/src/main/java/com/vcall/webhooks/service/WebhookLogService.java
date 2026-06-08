package com.vcall.webhooks.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.webhooks.dto.WebhookLogResponse;
import com.vcall.webhooks.entity.WebhookLog;
import com.vcall.webhooks.repository.WebhookLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WebhookLogService {

    private final WebhookLogRepository webhookLogRepository;

    @Transactional(readOnly = true)
    public List<WebhookLogResponse> getLogs(Long webhookId) {
        return webhookLogRepository.findByWebhookId(webhookId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WebhookLogResponse getLog(Long id) {
        WebhookLog log = webhookLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook log not found with id: " + id));
        return toResponse(log);
    }

    @Transactional
    public void deleteLog(Long id) {
        WebhookLog log = webhookLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook log not found with id: " + id));
        webhookLogRepository.delete(log);
    }

    private WebhookLogResponse toResponse(WebhookLog log) {
        return WebhookLogResponse.builder()
                .id(log.getId())
                .webhookId(log.getWebhookId())
                .event(log.getEvent())
                .requestBody(log.getRequestBody())
                .responseStatusCode(log.getResponseStatusCode())
                .responseBody(log.getResponseBody())
                .status(log.getStatus())
                .errorMessage(log.getErrorMessage())
                .executedAt(log.getExecutedAt())
                .build();
    }
}
