package com.vcall.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    @NotNull(message = "Source is required")
    private String source;

    private String category;

    @NotNull(message = "Priority is required")
    private String priority;

    private UUID assignedTo;

    private UUID relatedCallId;

    private UUID relatedConversationId;
}
