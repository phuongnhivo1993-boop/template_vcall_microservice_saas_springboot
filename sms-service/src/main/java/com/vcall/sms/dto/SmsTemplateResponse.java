package com.vcall.sms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsTemplateResponse {

    private Long id;
    private String name;
    private String content;
    private String variables;
    private boolean isActive;
    private LocalDateTime createdAt;
}
