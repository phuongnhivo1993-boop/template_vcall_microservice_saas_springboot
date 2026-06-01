package com.vcall.sms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsRequest {

    @NotBlank(message = "From number is required")
    private String fromNumber;

    @NotBlank(message = "To number is required")
    private String toNumber;

    @NotBlank(message = "Content is required")
    private String content;

    private Long templateId;

    private Map<String, String> variables;
}
