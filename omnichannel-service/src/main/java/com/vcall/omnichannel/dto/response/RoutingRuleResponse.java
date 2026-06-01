package com.vcall.omnichannel.dto.response;

import com.vcall.omnichannel.entity.Conversation.Channel;
import com.vcall.omnichannel.entity.OmnichannelRoutingRule.DestinationType;
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
public class RoutingRuleResponse {

    private Long id;
    private String name;
    private Channel channel;
    private String condition;
    private Integer priority;
    private DestinationType destinationType;
    private String destinationId;
    private Boolean isActive;
}
