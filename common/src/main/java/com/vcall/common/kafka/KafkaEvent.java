package com.vcall.common.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaEvent {
    private String id;
    private String topic;
    private String key;
    private String type;
    private String payload;
    private String source;
    private LocalDateTime timestamp;

    public static KafkaEvent create(String topic, String key, String type, String payload) {
        return KafkaEvent.builder()
                .id(UUID.randomUUID().toString())
                .topic(topic)
                .key(key)
                .type(type)
                .payload(payload)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
