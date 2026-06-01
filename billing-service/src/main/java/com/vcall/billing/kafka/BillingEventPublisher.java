package com.vcall.billing.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.billing.entity.Invoice;
import com.vcall.billing.entity.Subscription;
import com.vcall.common.kafka.KafkaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BillingEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.application.name}")
    private String source;

    public void publishInvoiceCreated(Invoice invoice) {
        publishEvent("invoice.created", invoice.getId().toString(), "INVOICE_CREATED", invoice);
    }

    public void publishPaymentCompleted(Invoice invoice) {
        publishEvent("payment.completed", invoice.getId().toString(), "PAYMENT_COMPLETED", invoice);
    }

    public void publishSubscriptionExpiring(Subscription subscription) {
        publishEvent("subscription.expiring", subscription.getId().toString(), "SUBSCRIPTION_EXPIRING", subscription);
    }

    private void publishEvent(String topic, String key, String eventType, Object data) {
        try {
            String payload = objectMapper.writeValueAsString(data);
            KafkaEvent event = KafkaEvent.create(topic, key, eventType, payload);
            event.setSource(source);
            kafkaTemplate.send(topic, key, event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize billing event: {}", eventType, e);
        }
    }
}
