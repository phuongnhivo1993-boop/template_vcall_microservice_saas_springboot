package com.vcall.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAccountResponse {

    private Long id;
    private String emailAddress;
    private String displayName;
    private String smtpHost;
    private Integer smtpPort;
    private Boolean isDefault;
}
