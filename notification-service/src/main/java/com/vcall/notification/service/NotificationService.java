package com.vcall.notification.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.notification.dto.BatchNotificationRequest;
import com.vcall.notification.dto.NotificationRequest;
import com.vcall.notification.dto.NotificationResponse;
import com.vcall.notification.entity.Notification;
import com.vcall.notification.entity.NotificationChannel;
import com.vcall.notification.entity.NotificationStatus;
import com.vcall.notification.entity.NotificationType;
import com.vcall.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailNotificationService emailNotificationService;
    private final SmsNotificationService smsNotificationService;
    private final PushNotificationService pushNotificationService;
    private final NotificationPreferenceService preferenceService;

    @Transactional
    public NotificationResponse sendNotification(NotificationRequest request) {
        boolean allowed = preferenceService.checkAllowed(request.getRecipientId(), request.getChannel(), request.getType());
        if (!allowed) {
            log.warn("Notification not allowed for user {} channel {} type {}", request.getRecipientId(), request.getChannel(), request.getType());
            throw new IllegalStateException("Notification preference disallows this notification");
        }

        Notification notification = new Notification();
        notification.setRecipientId(request.getRecipientId());
        notification.setRecipientAddress(request.getRecipientAddress());
        notification.setChannel(request.getChannel());
        notification.setType(request.getType());
        notification.setTitle(request.getTitle());
        notification.setBody(request.getBody());
        notification.setMetadata(request.getMetadata());
        notification.setStatus(NotificationStatus.PENDING);

        notification = notificationRepository.save(notification);

        try {
            switch (request.getChannel()) {
                case EMAIL -> emailNotificationService.sendEmail(notification);
                case SMS -> smsNotificationService.sendSms(notification);
                case PUSH -> pushNotificationService.sendPush(notification);
                case IN_APP -> {
                    notification.setStatus(NotificationStatus.SENT);
                    notification.setSentAt(LocalDateTime.now());
                }
            }
            if (notification.getStatus() == NotificationStatus.PENDING) {
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
            }
        } catch (Exception e) {
            log.error("Failed to send notification {}: {}", notification.getId(), e.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailureReason(e.getMessage());
        }

        notification = notificationRepository.save(notification);
        return toResponse(notification);
    }

    @Transactional
    public List<NotificationResponse> sendBatch(BatchNotificationRequest request) {
        return request.getRecipientIds().stream()
                .map(recipientId -> {
                    NotificationRequest single = NotificationRequest.builder()
                            .recipientId(recipientId)
                            .channel(request.getChannel())
                            .type(request.getType())
                            .title(request.getTitle())
                            .body(request.getBody())
                            .build();
                    return sendNotification(single);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(UUID recipientId, Pageable pageable) {
        return notificationRepository.findByRecipientIdOrderBySentAtDesc(recipientId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public NotificationResponse markAsRead(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(LocalDateTime.now());
        notification = notificationRepository.save(notification);
        return toResponse(notification);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getByRecipient(UUID recipientId, Pageable pageable) {
        return getNotifications(recipientId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUnreadByRecipient(UUID recipientId, Pageable pageable) {
        return notificationRepository.findByRecipientIdAndStatusNotOrderBySentAtDesc(recipientId, NotificationStatus.READ, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public void deleteNotification(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        notification.setIsDeleted(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void clearReadNotifications(UUID recipientId) {
        List<Notification> readNotifications = notificationRepository
                .findByRecipientIdAndStatus(recipientId, NotificationStatus.READ);
        for (Notification notification : readNotifications) {
            notification.setIsDeleted(true);
        }
        notificationRepository.saveAll(readNotifications);
    }

    @Transactional(readOnly = true)
    public NotificationResponse getById(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        return toResponse(notification);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getAllNotifications(Pageable pageable) {
        return notificationRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public NotificationResponse updateNotification(UUID id, NotificationRequest request) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        notification.setRecipientId(request.getRecipientId());
        notification.setRecipientAddress(request.getRecipientAddress());
        notification.setChannel(request.getChannel());
        notification.setType(request.getType());
        notification.setTitle(request.getTitle());
        notification.setBody(request.getBody());
        notification.setMetadata(request.getMetadata());
        notification = notificationRepository.save(notification);
        return toResponse(notification);
    }

    @Transactional
    public void bulkDeleteNotifications(List<UUID> ids) {
        List<Notification> notifications = notificationRepository.findAllById(ids);
        for (Notification notification : notifications) {
            notification.setIsDeleted(true);
        }
        notificationRepository.saveAll(notifications);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getNotificationStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalNotifications", notificationRepository.count());
        stats.put("pendingNotifications", notificationRepository.findByStatus(NotificationStatus.PENDING).size());
        stats.put("sentNotifications", notificationRepository.findByStatus(NotificationStatus.SENT).size());
        stats.put("failedNotifications", notificationRepository.findByStatus(NotificationStatus.FAILED).size());
        stats.put("readNotifications", notificationRepository.findByStatus(NotificationStatus.READ).size());
        return stats;
    }

    public Notification createAndSave(UUID recipientId, String recipientAddress, NotificationChannel channel,
                                       NotificationType type, String title, String body, String metadata) {
        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setRecipientAddress(recipientAddress);
        notification.setChannel(channel);
        notification.setType(type);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setMetadata(metadata);
        notification.setStatus(NotificationStatus.PENDING);
        return notificationRepository.save(notification);
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .recipientId(notification.getRecipientId())
                .recipientAddress(notification.getRecipientAddress())
                .channel(notification.getChannel())
                .type(notification.getType())
                .title(notification.getTitle())
                .body(notification.getBody())
                .status(notification.getStatus())
                .sentAt(notification.getSentAt())
                .build();
    }
}
