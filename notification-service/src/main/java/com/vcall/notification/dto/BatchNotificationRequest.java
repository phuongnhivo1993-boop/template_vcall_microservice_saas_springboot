package com.vcall.notification.dto;

import com.vcall.notification.entity.NotificationChannel;
import com.vcall.notification.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchNotificationRequest {

    @NotEmpty
    private List<UUID> recipientIds;

    @NotNull
    private NotificationChannel channel;

    @NotNull
    private NotificationType type;

    private String title;

    @NotBlank
    private String body;
}
