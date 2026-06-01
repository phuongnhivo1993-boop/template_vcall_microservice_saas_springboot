package com.vcall.sms.dto;

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
public class SmsProviderRequest {

    @NotBlank(message = "Provider name is required")
    private String name;

    @NotNull(message = "Provider type is required")
    private String providerType;

    private String apiUrl;
    private String apiKey;
    private String apiSecret;
    private String senderId;
    private boolean isDefault;
    private boolean isActive;
}
