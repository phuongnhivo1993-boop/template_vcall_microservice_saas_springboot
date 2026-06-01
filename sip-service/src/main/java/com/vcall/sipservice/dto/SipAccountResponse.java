package com.vcall.sipservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SipAccountResponse {
    private Long id;
    private String username;
    private String domain;
    private String realm;
    private String accountType;
    private String status;
    private Integer maxChannels;
    private Boolean allowRegistration;
    private UUID tenantId;
    private long devices;
}
