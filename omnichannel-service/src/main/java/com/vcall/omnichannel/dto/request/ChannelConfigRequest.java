package com.vcall.omnichannel.dto.request;

import com.vcall.omnichannel.entity.Conversation.Channel;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChannelConfigRequest {

    @NotNull
    private Channel channel;

    private Boolean isEnabled;

    private String config;
}
