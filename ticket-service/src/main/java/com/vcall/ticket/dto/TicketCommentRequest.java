package com.vcall.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketCommentRequest {

    @NotBlank(message = "Content is required")
    private String content;

    private Boolean isInternal = false;
}
