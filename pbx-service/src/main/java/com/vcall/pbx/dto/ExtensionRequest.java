package com.vcall.pbx.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtensionRequest {

    @NotBlank(message = "Extension number is required")
    @Size(max = 50)
    private String extensionNumber;

    @Size(max = 255)
    private String password;

    @Size(max = 255)
    private String displayName;

    @NotBlank(message = "Extension type is required")
    private String type;

    private Boolean voicemailEnabled;

    private String callForwarding;

    @Size(max = 50)
    private String outboundCallerId;

    private Integer maxConcurrentCalls;
}
