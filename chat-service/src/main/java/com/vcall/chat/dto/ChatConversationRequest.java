package com.vcall.chat.dto;

import com.vcall.chat.entity.ChatConversation.Source;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversationRequest {

    private UUID customerId;

    @NotNull(message = "Source is required")
    private Source source;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    private String customerEmail;

    private String pageUrl;

    private String userAgent;
}
