package com.vcall.sms.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.common.kafka.KafkaEvent;
import com.vcall.sms.entity.SmsCampaign;
import com.vcall.sms.entity.SmsMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SmsEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.application.name}")
    private String source;

    public void publishSmsSent(SmsMessage message) {
        publishEvent("sms.sent", message, "SMS_SENT", message.getMessageId());
    }

    public void publishSmsDelivered(SmsMessage message) {
        publishEvent("sms.delivered", message, "SMS_DELIVERED", message.getMessageId());
    }

    public void publishSmsFailed(SmsMessage message) {
        publishEvent("sms.failed", message, "SMS_FAILED", message.getMessageId());
    }

    public void publishCampaignStarted(SmsCampaign campaign) {
        publishEvent("campaign.started", campaign, "CAMPAIGN_STARTED", campaign.getId().toString());
    }

    public void publishCampaignFinished(SmsCampaign campaign) {
        publishEvent("campaign.finished", campaign, "CAMPAIGN_FINISHED", campaign.getId().toString());
    }

    private void publishEvent(String topic, Object data, String eventType, String key) {
        try {
            String payload = objectMapper.writeValueAsString(data);
            KafkaEvent event = KafkaEvent.create(topic, key, eventType, payload);
            event.setSource(source);
            kafkaTemplate.send(topic, key, event);
            log.info("Published event: {} to topic: {}", eventType, topic);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event: {}", eventType, e);
        }
    }
}
