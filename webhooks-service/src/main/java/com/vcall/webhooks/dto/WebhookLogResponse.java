package com.vcall.webhooks.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookLogResponse {

    private Long id;
    private Long webhookId;
    private String event;
    private String requestBody;
    private Integer responseStatusCode;
    private String responseBody;
    private String status;
    private String errorMessage;
    private LocalDateTime executedAt;
}
