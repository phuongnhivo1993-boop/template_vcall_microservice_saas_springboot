package com.vcall.sipservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SipAccountRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private String domain;

    private String realm;

    @NotBlank
    private String accountType;

    private Integer maxChannels;

    private Boolean allowRegistration;

    @NotNull
    private UUID tenantId;
}
