package com.vcall.recording.dto;

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
public class RecordingRequest {

    @NotNull(message = "Call ID is required")
    private UUID callId;

    private UUID agentId;

    private String customerNumber;

    @NotBlank(message = "Format is required")
    private String format;
}
