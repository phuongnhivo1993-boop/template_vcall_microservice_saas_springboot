package com.vcall.omnichannel.dto.request;

import com.vcall.omnichannel.entity.Conversation.Channel;
import com.vcall.omnichannel.entity.Conversation.Priority;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConversationRequest {

    @NotNull
    private Channel channel;

    private String externalId;

    @NotNull
    private UUID customerId;

    private String subject;

    private Priority priority;
}
