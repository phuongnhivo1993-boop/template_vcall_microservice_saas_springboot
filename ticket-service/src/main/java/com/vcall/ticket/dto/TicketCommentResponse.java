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
public class TicketCommentResponse {

    private Long id;
    private UUID ticketId;
    private String content;
    private UUID authorId;
    private String authorType;
    private Boolean isInternal;
    private LocalDateTime createdAt;
}
