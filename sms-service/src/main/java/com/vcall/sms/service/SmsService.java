package com.vcall.sms.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.common.exception.BadRequestException;
import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.sms.dto.SmsRequest;
import com.vcall.sms.dto.SmsResponse;
import com.vcall.sms.entity.SmsMessage;
import com.vcall.sms.entity.SmsTemplate;
import com.vcall.sms.kafka.SmsEventPublisher;
import com.vcall.sms.repository.SmsMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private final SmsMessageRepository smsMessageRepository;
    private final SmsTemplateService smsTemplateService;
    private final SmsEventPublisher smsEventPublisher;
    private final ObjectMapper objectMapper;

    @Transactional
    public SmsResponse sendSms(SmsRequest request) {
        String content = request.getContent();

        if (request.getTemplateId() != null) {
            content = smsTemplateService.renderTemplate(request.getTemplateId(), request.getVariables());
        }

        SmsMessage message = new SmsMessage();
        message.setMessageId(UUID.randomUUID().toString());
        message.setFromNumber(request.getFromNumber());
        message.setToNumber(request.getToNumber());
        message.setContent(content);
        message.setDirection(SmsMessage.SmsDirection.OUTBOUND);
        message.setStatus(SmsMessage.SmsStatus.PENDING);

        message = smsMessageRepository.save(message);

        try {
            // Simulate sending via provider (integration point)
            String gatewayResponse = simulateSend(message);
            message.setGatewayResponse(gatewayResponse);
            message.setStatus(SmsMessage.SmsStatus.SENT);
            message.setSentAt(LocalDateTime.now());
            message.setCost(BigDecimal.valueOf(0.05));
            message = smsMessageRepository.save(message);

            smsEventPublisher.publishSmsSent(message);
        } catch (Exception e) {
            log.error("Failed to send SMS: {}", e.getMessage());
            message.setStatus(SmsMessage.SmsStatus.FAILED);
            message.setGatewayResponse(e.getMessage());
            message = smsMessageRepository.save(message);
            smsEventPublisher.publishSmsFailed(message);
        }

        return toResponse(message);
    }

    @Transactional
    public List<SmsResponse> sendBatchSms(List<SmsRequest> requests) {
        return requests.stream()
                .map(this::sendSms)
                .collect(Collectors.toList());
    }

    public SmsResponse getSmsStatus(UUID id) {
        SmsMessage message = smsMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SMS not found with id: " + id));
        return toResponse(message);
    }

    public List<SmsResponse> getSmsHistory(String from, String to, LocalDateTime startDate, LocalDateTime endDate) {
        if (from != null && !from.isBlank()) {
            return smsMessageRepository.findByFromNumber(from).stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }
        if (to != null && !to.isBlank()) {
            return smsMessageRepository.findByToNumber(to).stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }
        if (startDate != null && endDate != null) {
            return smsMessageRepository.findBySentAtBetween(startDate, endDate).stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }
        return smsMessageRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void processDeliveryReport(String messageId, String status) {
        SmsMessage message = smsMessageRepository.findByMessageId(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("SMS not found with messageId: " + messageId));

        SmsMessage.SmsStatus newStatus = switch (status.toUpperCase()) {
            case "DELIVERED" -> SmsMessage.SmsStatus.DELIVERED;
            case "FAILED" -> SmsMessage.SmsStatus.FAILED;
            case "REJECTED" -> SmsMessage.SmsStatus.REJECTED;
            default -> throw new BadRequestException("Invalid delivery status: " + status);
        };

        message.setStatus(newStatus);
        if (newStatus == SmsMessage.SmsStatus.DELIVERED) {
            message.setDeliveredAt(LocalDateTime.now());
        }
        smsMessageRepository.save(message);

        if (newStatus == SmsMessage.SmsStatus.DELIVERED) {
            smsEventPublisher.publishSmsDelivered(message);
        } else {
            smsEventPublisher.publishSmsFailed(message);
        }
    }

    private String simulateSend(SmsMessage message) {
        // Integration point with actual SMS gateway
        return "{\"status\": \"sent\", \"provider_message_id\": \"" + UUID.randomUUID().toString() + "\"}";
    }

    private SmsResponse toResponse(SmsMessage message) {
        return SmsResponse.builder()
                .id(message.getId())
                .messageId(message.getMessageId())
                .fromNumber(message.getFromNumber())
                .toNumber(message.getToNumber())
                .content(message.getContent())
                .status(message.getStatus().name())
                .sentAt(message.getSentAt())
                .cost(message.getCost())
                .build();
    }
}
