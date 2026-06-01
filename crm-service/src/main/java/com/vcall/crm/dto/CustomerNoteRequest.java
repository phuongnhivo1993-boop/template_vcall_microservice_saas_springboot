package com.vcall.crm.dto;

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
public class CustomerNoteRequest {

    @NotNull
    private UUID customerId;

    private UUID leadId;

    @NotBlank
    private String title;

    private String content;

    private boolean isPinned;
}
