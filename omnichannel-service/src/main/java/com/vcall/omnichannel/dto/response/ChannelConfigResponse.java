package com.vcall.omnichannel.dto.response;

import com.vcall.omnichannel.entity.Conversation.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelConfigResponse {

    private Long id;
    private Channel channel;
    private Boolean isEnabled;
    private String config;
}
