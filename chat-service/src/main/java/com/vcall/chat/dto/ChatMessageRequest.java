package com.vcall.chat.dto;

import com.vcall.chat.entity.ChatMessage.ContentType;
import com.vcall.chat.entity.ChatMessage.SenderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {

    @NotNull(message = "Sender type is required")
    private SenderType senderType;

    @NotBlank(message = "Sender ID is required")
    private String senderId;

    @NotBlank(message = "Content is required")
    private String content;

    private ContentType contentType = ContentType.TEXT;
}
