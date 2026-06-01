package com.vcall.notification.dto;

import com.vcall.notification.entity.NotificationChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateResponse {

    private Long id;
    private String name;
    private NotificationChannel channel;
    private String title;
    private String body;
    private String variables;
    private Boolean isActive;
}
