package com.vcall.notification.dto;

import com.vcall.notification.entity.NotificationChannel;
import com.vcall.notification.entity.NotificationStatus;
import com.vcall.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private UUID id;
    private UUID recipientId;
    private String recipientAddress;
    private NotificationChannel channel;
    private NotificationType type;
    private String title;
    private String body;
    private NotificationStatus status;
    private LocalDateTime sentAt;
}
