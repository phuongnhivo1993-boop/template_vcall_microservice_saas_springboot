package com.vcall.email.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.email.dto.EmailRequest;
import com.vcall.email.dto.EmailResponse;
import com.vcall.email.entity.EmailAttachment;
import com.vcall.email.entity.EmailMessage;
import com.vcall.email.kafka.EmailEventPublisher;
import com.vcall.email.repository.EmailMessageRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final EmailMessageRepository emailMessageRepository;
    private final EmailEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Transactional
    public EmailResponse sendEmail(EmailRequest request) {
        EmailMessage email = new EmailMessage();
        email.setMessageId(UUID.randomUUID().toString());
        email.setConversationId(UUID.randomUUID());
        email.setToAddresses(toJson(request.getToAddresses()));
        email.setCcAddresses(toJson(request.getCcAddresses()));
        email.setBccAddresses(toJson(request.getBccAddresses()));
        email.setSubject(request.getSubject());
        email.setBodyHtml(request.getBodyHtml());
        email.setBodyText(request.getBodyText());
        email.setDirection(EmailMessage.EmailDirection.OUTBOUND);
        email.setStatus(EmailMessage.EmailStatus.SENT);
        email.setSentAt(LocalDateTime.now());

        email = emailMessageRepository.save(email);
        eventPublisher.publishEmailSent(email);
        return toResponse(email);
    }

    @Transactional
    public EmailResponse receiveEmail(EmailMessage email) {
        email.setDirection(EmailMessage.EmailDirection.INBOUND);
        email.setStatus(EmailMessage.EmailStatus.RECEIVED);
        email.setReceivedAt(LocalDateTime.now());

        email = emailMessageRepository.save(email);
        eventPublisher.publishEmailReceived(email);
        return toResponse(email);
    }

    @Transactional
    public void deleteEmail(UUID id) {
        EmailMessage email = emailMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found with id: " + id));
        email.setIsDeleted(true);
        emailMessageRepository.save(email);

        eventPublisher.publishEmailDeleted(email);
    }

    @Transactional(readOnly = true)
    public EmailResponse getEmail(UUID id) {
        EmailMessage email = emailMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found with id: " + id));
        return toResponse(email);
    }

    @Transactional(readOnly = true)
    public Page<EmailResponse> getEmails(Pageable pageable) {
        return emailMessageRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<EmailResponse> getConversationThread(UUID conversationId) {
        return emailMessageRepository.findByConversationIdOrderByReceivedAt(conversationId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmailResponse> searchEmails(String query) {
        Specification<EmailMessage> spec = (root, criteriaQuery, criteriaBuilder) -> {
            String pattern = "%" + query.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("subject")), pattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("bodyText")), pattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("fromAddress")), pattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("toAddresses")), pattern));
            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
        return emailMessageRepository.findAll(spec).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private String toJson(List<String> values) {
        if (values == null) return null;
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize addresses", e);
        }
    }

    private EmailResponse toResponse(EmailMessage email) {
        return EmailResponse.builder()
                .id(email.getId())
                .messageId(email.getMessageId())
                .fromAddress(email.getFromAddress())
                .toAddresses(email.getToAddresses())
                .subject(email.getSubject())
                .bodyText(email.getBodyText())
                .direction(email.getDirection().name())
                .status(email.getStatus().name())
                .sentAt(email.getSentAt())
                .receivedAt(email.getReceivedAt())
                .build();
    }
}
