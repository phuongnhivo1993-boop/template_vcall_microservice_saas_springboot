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
public class ChatMessageResponse {

    private UUID id;
    private UUID conversationId;
    private String senderType;
    private String senderId;
    private String content;
    private String contentType;
    private LocalDateTime sentAt;
    private boolean isRead;
}
