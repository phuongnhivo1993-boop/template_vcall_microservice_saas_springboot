package com.vcall.notification.dto;

import com.vcall.notification.entity.NotificationChannel;
import com.vcall.notification.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotNull
    private UUID recipientId;

    private String recipientAddress;

    @NotNull
    private NotificationChannel channel;

    @NotNull
    private NotificationType type;

    private String title;

    @NotBlank
    private String body;

    private String metadata;
}
