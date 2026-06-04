package com.vcall.webhooks.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WebhookRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "URL is required")
    private String url;

    private String events;

    private Boolean isActive;
}
