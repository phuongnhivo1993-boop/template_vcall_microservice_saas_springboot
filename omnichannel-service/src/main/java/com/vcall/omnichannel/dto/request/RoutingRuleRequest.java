package com.vcall.omnichannel.dto.request;

import com.vcall.omnichannel.entity.Conversation.Channel;
import com.vcall.omnichannel.entity.OmnichannelRoutingRule.DestinationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoutingRuleRequest {

    @NotNull
    private String name;

    @NotNull
    private Channel channel;

    private String condition;

    private Integer priority;

    @NotNull
    private DestinationType destinationType;

    private String destinationId;

    private Boolean isActive;
}
