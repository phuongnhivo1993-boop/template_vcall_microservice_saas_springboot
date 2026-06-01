package com.vcall.notification.dto;

import com.vcall.notification.entity.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateRequest {

    @NotBlank
    private String name;

    @NotNull
    private NotificationChannel channel;

    private String title;

    @NotBlank
    private String body;

    private String variables;

    private Boolean isActive;
}
