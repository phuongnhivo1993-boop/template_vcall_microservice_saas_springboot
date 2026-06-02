package com.vcall.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversationUpdateRequest {

    private String customerName;
    private String customerEmail;
    private String pageUrl;
    private String userAgent;
}
