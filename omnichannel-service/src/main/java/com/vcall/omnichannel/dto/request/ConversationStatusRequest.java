package com.vcall.omnichannel.dto.request;

import com.vcall.omnichannel.entity.Conversation.ConversationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConversationStatusRequest {

    @NotNull
    private ConversationStatus status;
}
