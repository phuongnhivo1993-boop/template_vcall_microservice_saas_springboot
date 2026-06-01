package com.vcall.omnichannel.dto.response;

import com.vcall.omnichannel.entity.Conversation.Channel;
import com.vcall.omnichannel.entity.Conversation.ConversationStatus;
import com.vcall.omnichannel.entity.Conversation.Priority;
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
public class ConversationResponse {

    private UUID id;
    private Channel channel;
    private String externalId;
    private UUID customerId;
    private String customerName;
    private UUID agentId;
    private String agentName;
    private ConversationStatus status;
    private Priority priority;
    private String subject;
    private LocalDateTime startedAt;
    private LocalDateTime closedAt;
    private long messageCount;
    private String lastMessage;
}
