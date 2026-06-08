package com.vcall.webhooks.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.webhooks.dto.TestResultResponse;
import com.vcall.webhooks.dto.WebhookRequest;
import com.vcall.webhooks.dto.WebhookResponse;
import com.vcall.webhooks.entity.Webhook;
import com.vcall.webhooks.entity.WebhookLog;
import com.vcall.webhooks.repository.WebhookLogRepository;
import com.vcall.webhooks.repository.WebhookRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private final WebhookRepository webhookRepository;
    private final WebhookLogRepository webhookLogRepository;

    @Transactional(readOnly = true)
    public Page<WebhookResponse> getAllWebhooks(Pageable pageable) {
        return webhookRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public WebhookResponse getWebhook(Long id) {
        Webhook webhook = webhookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook not found with id: " + id));
        return toResponse(webhook);
    }

    @Transactional
    public WebhookResponse createWebhook(WebhookRequest request) {
        Webhook webhook = new Webhook();
        webhook.setName(request.getName());
        webhook.setUrl(request.getUrl());
        webhook.setEvents(request.getEvents());
        webhook.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        webhook.setSecretKey(UUID.randomUUID().toString());
        webhook = webhookRepository.save(webhook);
        return toResponse(webhook);
    }

    @Transactional
    public WebhookResponse updateWebhook(Long id, WebhookRequest request) {
        Webhook webhook = webhookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook not found with id: " + id));
        webhook.setName(request.getName());
        webhook.setUrl(request.getUrl());
        webhook.setEvents(request.getEvents());
        if (request.getIsActive() != null) {
            webhook.setIsActive(request.getIsActive());
        }
        webhook = webhookRepository.save(webhook);
        return toResponse(webhook);
    }

    @Transactional
    public void deleteWebhook(Long id) {
        Webhook webhook = webhookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook not found with id: " + id));
        webhook.setIsDeleted(true);
        webhookRepository.save(webhook);
    }

    @Transactional
    public WebhookResponse duplicateWebhook(Long id) {
        Webhook original = webhookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook not found with id: " + id));
        Webhook copy = new Webhook();
        copy.setName(original.getName() + " (Copy)");
        copy.setUrl(original.getUrl());
        copy.setEvents(original.getEvents());
        copy.setIsActive(false);
        copy.setSecretKey(java.util.UUID.randomUUID().toString());
        copy = webhookRepository.save(copy);
        return toResponse(copy);
    }

    @Transactional
    public TestResultResponse testWebhook(Long id) {
        Webhook webhook = webhookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook not found with id: " + id));

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("{\"test\": true, \"event\": \"webhook.test\"}", headers);

        WebhookLog logEntry = new WebhookLog();
        logEntry.setWebhookId(webhook.getId());
        logEntry.setEvent("webhook.test");
        logEntry.setRequestBody("{\"test\": true, \"event\": \"webhook.test\"}");
        logEntry.setStatus("PENDING");
        logEntry.setExecutedAt(LocalDateTime.now());
        logEntry = webhookLogRepository.save(logEntry);

        TestResultResponse result = new TestResultResponse();

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    webhook.getUrl(), HttpMethod.POST, entity, String.class);
            logEntry.setStatus("SUCCESS");
            logEntry.setResponseStatusCode(response.getStatusCode().value());
            logEntry.setResponseBody(response.getBody());
            webhook.setLastTriggeredAt(LocalDateTime.now());
            webhookRepository.save(webhook);
            result.setSuccess(true);
            result.setStatusCode(response.getStatusCode().value());
            result.setMessage("Webhook test successful");
        } catch (Exception e) {
            logEntry.setStatus("FAILURE");
            logEntry.setErrorMessage(e.getMessage());
            result.setSuccess(false);
            result.setStatusCode(0);
            result.setMessage("Webhook test failed: " + e.getMessage());
        }

        webhookLogRepository.save(logEntry);
        return result;
    }

    @Async
    @Transactional
    public void triggerWebhook(String event, String payload) {
        List<Webhook> webhooks = webhookRepository.findByIsActiveTrue();
        for (Webhook webhook : webhooks) {
            if (webhook.getEvents() != null && webhook.getEvents().contains(event)) {
                sendWebhook(webhook, event, payload);
            }
        }
    }

    @Transactional
    public void bulkDeleteWebhooks(List<Long> ids) {
        List<Webhook> webhooks = webhookRepository.findAllById(ids);
        for (Webhook webhook : webhooks) {
            webhook.setIsDeleted(true);
        }
        webhookRepository.saveAll(webhooks);
    }

    @Transactional(readOnly = true)
    public Page<WebhookResponse> searchWebhooks(String name, String event, Boolean isActive, Pageable pageable) {
        Specification<Webhook> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.isEmpty()) {
                predicates.add(cb.like(root.get("name"), "%" + name + "%"));
            }
            if (event != null && !event.isEmpty()) {
                predicates.add(cb.like(root.get("events"), "%" + event + "%"));
            }
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return webhookRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getWebhookStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalWebhooks", webhookRepository.count());
        stats.put("activeWebhooks", webhookRepository.findByIsActiveTrue().size());
        stats.put("totalLogs", webhookLogRepository.count());
        return stats;
    }

    private void sendWebhook(Webhook webhook, String event, String payload) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        WebhookLog logEntry = new WebhookLog();
        logEntry.setWebhookId(webhook.getId());
        logEntry.setEvent(event);
        logEntry.setRequestBody(payload);
        logEntry.setStatus("PENDING");
        logEntry.setExecutedAt(LocalDateTime.now());
        logEntry = webhookLogRepository.save(logEntry);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    webhook.getUrl(), HttpMethod.POST, entity, String.class);
            logEntry.setStatus("SUCCESS");
            logEntry.setResponseStatusCode(response.getStatusCode().value());
            logEntry.setResponseBody(response.getBody());
            webhook.setLastTriggeredAt(LocalDateTime.now());
            webhookRepository.save(webhook);
        } catch (Exception e) {
            logEntry.setStatus("FAILURE");
            logEntry.setErrorMessage(e.getMessage());
            log.error("Failed to send webhook {} for event {}: {}", webhook.getId(), event, e.getMessage());
        }

        webhookLogRepository.save(logEntry);
    }

    private WebhookResponse toResponse(Webhook webhook) {
        return WebhookResponse.builder()
                .id(webhook.getId())
                .name(webhook.getName())
                .url(webhook.getUrl())
                .events(webhook.getEvents())
                .isActive(webhook.getIsActive())
                .createdAt(webhook.getCreatedAt())
                .updatedAt(webhook.getUpdatedAt())
                .build();
    }
}
