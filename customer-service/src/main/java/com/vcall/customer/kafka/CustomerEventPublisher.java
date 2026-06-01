package com.vcall.customer.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.common.kafka.KafkaEvent;
import com.vcall.customer.dto.CustomerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.application.name:customer-service}")
    private String source;

    public void publishCustomerCreated(CustomerResponse customer) {
        try {
            String payload = objectMapper.writeValueAsString(customer);
            KafkaEvent event = KafkaEvent.create("customer.created", customer.getId().toString(), "CUSTOMER_CREATED", payload);
            event.setSource(source);
            kafkaTemplate.send("customer.created", customer.getId().toString(), event);
            log.info("Published customer.created event for customer: {}", customer.getId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize customer created event", e);
        }
    }

    public void publishCustomerUpdated(CustomerResponse customer) {
        try {
            String payload = objectMapper.writeValueAsString(customer);
            KafkaEvent event = KafkaEvent.create("customer.updated", customer.getId().toString(), "CUSTOMER_UPDATED", payload);
            event.setSource(source);
            kafkaTemplate.send("customer.updated", customer.getId().toString(), event);
            log.info("Published customer.updated event for customer: {}", customer.getId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize customer updated event", e);
        }
    }
}
