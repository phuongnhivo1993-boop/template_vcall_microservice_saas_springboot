package com.vcall.sipservice.dto;

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
public class SipRegistrationRequest {

    @NotNull(message = "SIP account ID is required")
    private Long sipAccountId;

    @NotBlank(message = "Contact URI is required")
    private String contactUri;

    private String userAgent;

    private String ipAddress;

    private Integer port;

    @NotBlank(message = "Transport is required")
    private String transport;

    private Integer expires;
}
