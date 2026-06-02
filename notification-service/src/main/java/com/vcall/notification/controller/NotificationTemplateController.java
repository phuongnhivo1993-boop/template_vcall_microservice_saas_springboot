package com.vcall.notification.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.notification.dto.NotificationTemplateRequest;
import com.vcall.notification.dto.NotificationTemplateResponse;
import com.vcall.notification.service.NotificationTemplateService;
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
@RequestMapping("/api/v1/notification-templates")
@RequiredArgsConstructor
public class NotificationTemplateController {

    private final NotificationTemplateService templateService;

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> create(@Valid @RequestBody NotificationTemplateRequest request) {
        NotificationTemplateResponse response = templateService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Template created", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationTemplateResponse>>> getAll(Pageable pageable) {
        Page<NotificationTemplateResponse> templates = templateService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(templates));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> getById(@PathVariable Long id) {
        NotificationTemplateResponse response = templateService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> update(@PathVariable Long id,
                                                                              @Valid @RequestBody NotificationTemplateRequest request) {
        NotificationTemplateResponse response = templateService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Template updated", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        templateService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Template deleted", null));
    }
}
