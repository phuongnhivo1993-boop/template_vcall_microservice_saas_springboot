package com.vcall.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAccountRequest {

    @NotBlank
    @Email
    private String emailAddress;

    private String displayName;

    @NotBlank
    private String smtpHost;

    private Integer smtpPort;

    private String smtpUsername;

    private String smtpPassword;

    private String imapHost;

    private Integer imapPort;

    private Boolean useSSL;

    private Boolean isDefault;
}
