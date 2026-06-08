package com.vcall.webhooks.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.webhooks.dto.WebhookLogResponse;
import com.vcall.webhooks.service.WebhookLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/webhooks/{webhookId}/logs")
@RequiredArgsConstructor
public class WebhookLogController {

    private final WebhookLogService webhookLogService;

    @GetMapping
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<WebhookLogResponse>>> getLogs(
            @PathVariable Long webhookId) {
        List<WebhookLogResponse> logs = webhookLogService.getLogs(webhookId);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<WebhookLogResponse>> getLog(@PathVariable Long id) {
        WebhookLogResponse log = webhookLogService.getLog(id);
        return ResponseEntity.ok(ApiResponse.success(log));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Void>> deleteLog(@PathVariable Long id) {
        webhookLogService.deleteLog(id);
        return ResponseEntity.ok(ApiResponse.success("Webhook log deleted successfully", null));
    }
}
