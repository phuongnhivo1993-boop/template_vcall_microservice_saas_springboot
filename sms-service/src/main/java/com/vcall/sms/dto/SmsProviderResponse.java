package com.vcall.sms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsProviderResponse {

    private Long id;
    private String name;
    private String providerType;
    private String senderId;
    private boolean isDefault;
    private boolean isActive;
}
