package com.vcall.notification.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.notification.dto.BatchNotificationRequest;
import com.vcall.notification.dto.NotificationRequest;
import com.vcall.notification.dto.NotificationResponse;
import com.vcall.notification.dto.NotificationTemplateResponse;
import com.vcall.notification.service.NotificationService;
import com.vcall.notification.service.NotificationTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationTemplateService templateService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<NotificationResponse>> send(@Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.sendNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Notification sent", response));
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> sendBatch(@Valid @RequestBody BatchNotificationRequest request) {
        List<NotificationResponse> responses = notificationService.sendBatch(request);
        return ResponseEntity.ok(ApiResponse.success("Batch notifications sent", responses));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable UUID id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted", null));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearRead(@RequestParam UUID recipientId) {
        notificationService.clearReadNotifications(recipientId);
        return ResponseEntity.ok(ApiResponse.success("Read notifications cleared", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getById(@PathVariable UUID id) {
        NotificationResponse response = notificationService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/recipient/{recipientId}")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getByRecipient(@PathVariable UUID recipientId,
                                                                                   Pageable pageable) {
        Page<NotificationResponse> notifications = notificationService.getByRecipient(recipientId, pageable);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable UUID id) {
        NotificationResponse response = notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", response));
    }

    @GetMapping("/unread/{recipientId}")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getUnread(@PathVariable UUID recipientId,
                                                                               Pageable pageable) {
        Page<NotificationResponse> notifications = notificationService.getUnreadByRecipient(recipientId, pageable);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getAllNotifications(
            @RequestParam(required = false) UUID recipientId, Pageable pageable) {
        Page<NotificationResponse> notifications;
        if (recipientId != null) {
            notifications = notificationService.getByRecipient(recipientId, pageable);
        } else {
            notifications = notificationService.getAllNotifications(pageable);
        }
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationResponse>> updateNotification(
            @PathVariable UUID id, @Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.updateNotification(id, request);
        return ResponseEntity.ok(ApiResponse.success("Notification updated successfully", response));
    }

    @PostMapping("/bulk-delete")
    public ResponseEntity<ApiResponse<Void>> bulkDeleteNotifications(@RequestBody List<UUID> ids) {
        notificationService.bulkDeleteNotifications(ids);
        return ResponseEntity.ok(ApiResponse.success("Notifications deleted successfully", null));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNotificationStats() {
        Map<String, Object> stats = notificationService.getNotificationStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<Page<NotificationTemplateResponse>>> getTemplates(Pageable pageable) {
        Page<NotificationTemplateResponse> templates = templateService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(templates));
    }
}
