package com.vcall.webhooks.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.webhooks.dto.TestResultResponse;
import com.vcall.webhooks.dto.WebhookRequest;
import com.vcall.webhooks.dto.WebhookResponse;
import com.vcall.webhooks.service.WebhookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<WebhookResponse>>> getAllWebhooks(Pageable pageable) {
        Page<WebhookResponse> page = webhookService.getAllWebhooks(pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WebhookResponse>> getWebhook(@PathVariable Long id) {
        WebhookResponse response = webhookService.getWebhook(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WebhookResponse>> createWebhook(
            @Valid @RequestBody WebhookRequest request) {
        WebhookResponse response = webhookService.createWebhook(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Webhook created successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WebhookResponse>> updateWebhook(
            @PathVariable Long id,
            @Valid @RequestBody WebhookRequest request) {
        WebhookResponse response = webhookService.updateWebhook(id, request);
        return ResponseEntity.ok(ApiResponse.success("Webhook updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWebhook(@PathVariable Long id) {
        webhookService.deleteWebhook(id);
        return ResponseEntity.ok(ApiResponse.success("Webhook deleted successfully", null));
    }

    @PostMapping("/{id}/test")
    public ResponseEntity<ApiResponse<TestResultResponse>> testWebhook(@PathVariable Long id) {
        TestResultResponse result = webhookService.testWebhook(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
