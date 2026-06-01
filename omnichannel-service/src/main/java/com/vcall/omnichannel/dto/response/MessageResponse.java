package com.vcall.omnichannel.dto.response;

import com.vcall.omnichannel.entity.Message.ContentType;
import com.vcall.omnichannel.entity.Message.SenderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {

    private UUID id;
    private UUID conversationId;
    private SenderType senderType;
    private String senderId;
    private String content;
    private ContentType contentType;
    private String attachmentUrl;
    private LocalDateTime sentAt;
    private Boolean isRead;
}
