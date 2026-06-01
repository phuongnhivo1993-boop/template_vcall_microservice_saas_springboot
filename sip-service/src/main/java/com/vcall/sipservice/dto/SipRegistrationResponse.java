package com.vcall.sipservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SipRegistrationResponse {
    private Long id;
    private Long sipAccountId;
    private String contactUri;
    private String userAgent;
    private String ipAddress;
    private Integer port;
    private String transport;
    private LocalDateTime registeredAt;
    private LocalDateTime lastRefresh;
    private String status;
}
