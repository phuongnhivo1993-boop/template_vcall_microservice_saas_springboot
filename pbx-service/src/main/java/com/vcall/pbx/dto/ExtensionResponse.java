package com.vcall.pbx.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtensionResponse {

    private Long id;
    private String extensionNumber;
    private String displayName;
    private String type;
    private String status;
    private Boolean voicemailEnabled;
    private String callForwarding;
    private String outboundCallerId;
    private Integer maxConcurrentCalls;
    private LocalDateTime createdAt;
}
