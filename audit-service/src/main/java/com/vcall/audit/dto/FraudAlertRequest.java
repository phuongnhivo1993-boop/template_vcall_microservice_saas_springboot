package com.vcall.audit.dto;

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
public class FraudAlertRequest {

    @NotBlank
    private String alertType;

    @NotBlank
    private String severity;

    @NotNull
    private UUID actorId;

    private String description;

    private String evidence;
}
