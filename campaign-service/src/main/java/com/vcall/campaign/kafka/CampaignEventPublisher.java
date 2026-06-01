package com.vcall.campaign.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.campaign.entity.Campaign;
import com.vcall.campaign.entity.CampaignMember;
import com.vcall.common.kafka.KafkaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CampaignEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.application.name}")
    private String source;

    public void publishCampaignStarted(Campaign campaign) {
        try {
            String payload = objectMapper.writeValueAsString(campaign);
            KafkaEvent event = KafkaEvent.create("campaign.started", campaign.getId().toString(),
                    "CAMPAIGN_STARTED", payload);
            event.setSource(source);
            kafkaTemplate.send("campaign.started", campaign.getId().toString(), event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize campaign started event", e);
        }
    }

    public void publishCampaignFinished(Campaign campaign) {
        try {
            String payload = objectMapper.writeValueAsString(campaign);
            KafkaEvent event = KafkaEvent.create("campaign.finished", campaign.getId().toString(),
                    "CAMPAIGN_FINISHED", payload);
            event.setSource(source);
            kafkaTemplate.send("campaign.finished", campaign.getId().toString(), event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize campaign finished event", e);
        }
    }

    public void publishMemberCompleted(CampaignMember member) {
        try {
            String payload = objectMapper.writeValueAsString(member);
            KafkaEvent event = KafkaEvent.create("campaign.member.completed", member.getId().toString(),
                    "CAMPAIGN_MEMBER_COMPLETED", payload);
            event.setSource(source);
            kafkaTemplate.send("campaign.member.completed", member.getId().toString(), event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize member completed event", e);
        }
    }
}
