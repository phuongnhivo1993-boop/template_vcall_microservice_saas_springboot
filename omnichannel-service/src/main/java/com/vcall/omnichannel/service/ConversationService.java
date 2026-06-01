package com.vcall.omnichannel.service;

import com.vcall.omnichannel.dto.request.ConversationAssignRequest;
import com.vcall.omnichannel.dto.request.ConversationRequest;
import com.vcall.omnichannel.dto.request.ConversationStatusRequest;
import com.vcall.omnichannel.dto.response.ConversationResponse;
import com.vcall.omnichannel.entity.Conversation;
import com.vcall.omnichannel.entity.Conversation.Channel;
import com.vcall.omnichannel.entity.Conversation.ConversationStatus;
import com.vcall.omnichannel.kafka.OmnichannelEventPublisher;
import com.vcall.omnichannel.repository.ConversationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageService messageService;
    private final OmnichannelEventPublisher eventPublisher;

    @Transactional
    public ConversationResponse createConversation(ConversationRequest request) {
        Conversation conversation = new Conversation();
        conversation.setChannel(request.getChannel());
        conversation.setExternalId(request.getExternalId());
        conversation.setCustomerId(request.getCustomerId());
        conversation.setSubject(request.getSubject());
        conversation.setPriority(request.getPriority() != null ? request.getPriority() : Conversation.Priority.MEDIUM);
        conversation.setStatus(ConversationStatus.PENDING);
        conversation.setStartedAt(LocalDateTime.now());

        conversation = conversationRepository.save(conversation);

        eventPublisher.publishConversationCreated(conversation);

        return toResponse(conversation);
    }

    @Transactional
    public ConversationResponse assignAgent(UUID conversationId, ConversationAssignRequest request) {
        Conversation conversation = findById(conversationId);
        conversation.setAgentId(request.getAgentId());
        conversation.setAssignedAt(LocalDateTime.now());
        conversation.setStatus(ConversationStatus.ACTIVE);

        conversation = conversationRepository.save(conversation);

        eventPublisher.publishConversationAssigned(conversation);

        return toResponse(conversation);
    }

    @Transactional
    public ConversationResponse updateStatus(UUID conversationId, ConversationStatusRequest request) {
        Conversation conversation = findById(conversationId);
        conversation.setStatus(request.getStatus());

        if (request.getStatus() == ConversationStatus.CLOSED) {
            conversation.setClosedAt(LocalDateTime.now());
            conversation = conversationRepository.save(conversation);
            eventPublisher.publishConversationClosed(conversation);
        } else {
            conversation = conversationRepository.save(conversation);
        }

        return toResponse(conversation);
    }

    @Transactional(readOnly = true)
    public ConversationResponse getById(UUID conversationId) {
        return toResponse(findById(conversationId));
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getByChannel(Channel channel) {
        return conversationRepository.findByChannel(channel)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getByCustomer(UUID customerId) {
        return conversationRepository.findByCustomerId(customerId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> search(Channel channel, ConversationStatus status, UUID agentId) {
        List<Conversation> conversations;

        if (channel != null) {
            conversations = conversationRepository.findByChannel(channel);
        } else if (status != null) {
            conversations = conversationRepository.findByStatus(status);
        } else if (agentId != null) {
            conversations = conversationRepository.findByAgentId(agentId);
        } else {
            conversations = conversationRepository.findAll();
        }

        return conversations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private Conversation findById(UUID id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found with id: " + id));
    }

    private ConversationResponse toResponse(Conversation conversation) {
        long messageCount = messageService.getMessageCount(conversation.getId());
        String lastMessage = messageService.getLastMessageContent(conversation.getId());

        return ConversationResponse.builder()
                .id(conversation.getId())
                .channel(conversation.getChannel())
                .externalId(conversation.getExternalId())
                .customerId(conversation.getCustomerId())
                .agentId(conversation.getAgentId())
                .status(conversation.getStatus())
                .priority(conversation.getPriority())
                .subject(conversation.getSubject())
                .startedAt(conversation.getStartedAt())
                .closedAt(conversation.getClosedAt())
                .messageCount(messageCount)
                .lastMessage(lastMessage)
                .build();
    }
}
