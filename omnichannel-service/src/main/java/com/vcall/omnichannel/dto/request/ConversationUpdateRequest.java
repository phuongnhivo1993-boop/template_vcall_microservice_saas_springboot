package com.vcall.omnichannel.dto.request;

import com.vcall.omnichannel.entity.Conversation.Priority;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConversationUpdateRequest {

    private String subject;
    private Priority priority;
}
