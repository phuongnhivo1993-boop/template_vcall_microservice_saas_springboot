package com.vcall.chat.dto;

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
public class ChatConversationResponse {

    private UUID id;
    private String conversationId;
    private UUID customerId;
    private UUID agentId;
    private String source;
    private String status;
    private LocalDateTime assignedAt;
    private LocalDateTime closedAt;
    private String customerName;
    private int messageCount;
    private String lastMessage;
    private LocalDateTime createdAt;
}
