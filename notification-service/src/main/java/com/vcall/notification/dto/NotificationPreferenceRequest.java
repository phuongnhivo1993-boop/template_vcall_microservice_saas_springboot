package com.vcall.notification.dto;

import com.vcall.notification.entity.NotificationChannel;
import com.vcall.notification.entity.NotificationType;
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
public class NotificationPreferenceRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private NotificationChannel channel;

    @NotNull
    private NotificationType type;

    @NotNull
    private Boolean isEnabled;
}
