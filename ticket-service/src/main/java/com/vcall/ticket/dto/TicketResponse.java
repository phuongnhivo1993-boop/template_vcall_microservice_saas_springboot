package com.vcall.ticket.dto;

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
public class TicketResponse {

    private UUID id;
    private String ticketNumber;
    private String title;
    private String description;
    private UUID customerId;
    private String source;
    private String category;
    private String priority;
    private String status;
    private UUID assignedTo;
    private LocalDateTime firstResponseAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
    private String slaStatus;
    private LocalDateTime createdAt;
}
