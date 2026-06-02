package com.vcall.customer360.kafka;

import com.vcall.customer360.service.Customer360Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class Customer360EventConsumer {

    private final Customer360Service customer360Service;

    @KafkaListener(topics = "customer.events", groupId = "customer360-group")
    public void handleCustomerEvent(Map<String, Object> event) {
        try {
            String eventType = (String) event.get("eventType");
            UUID customerId = UUID.fromString((String) event.get("customerId"));
            log.info("Received customer event: {} for customer: {}", eventType, customerId);
            customer360Service.updateProfile(customerId, event);
        } catch (Exception e) {
            log.error("Failed to process customer event", e);
        }
    }
}
