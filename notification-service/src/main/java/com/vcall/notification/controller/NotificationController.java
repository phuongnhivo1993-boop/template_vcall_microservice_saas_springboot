package com.vcall.notification.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.notification.dto.BatchNotificationRequest;
import com.vcall.notification.dto.NotificationRequest;
import com.vcall.notification.dto.NotificationResponse;
import com.vcall.notification.service.NotificationService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

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
}
