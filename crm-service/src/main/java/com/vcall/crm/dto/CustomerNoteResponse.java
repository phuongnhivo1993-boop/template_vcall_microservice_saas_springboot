package com.vcall.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerNoteResponse {

    private Long id;
    private UUID customerId;
    private UUID leadId;
    private String title;
    private String content;
    private boolean isPinned;
    private LocalDateTime createdAt;
}
