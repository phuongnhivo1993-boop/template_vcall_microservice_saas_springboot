package com.vcall.omnichannel.service;

import com.vcall.omnichannel.dto.request.ConversationAssignRequest;
import com.vcall.omnichannel.dto.request.ConversationRequest;
import com.vcall.omnichannel.dto.request.ConversationStatusRequest;
import com.vcall.omnichannel.dto.request.ConversationUpdateRequest;
import com.vcall.omnichannel.dto.response.ConversationResponse;
import com.vcall.omnichannel.entity.Conversation;
import com.vcall.omnichannel.entity.Conversation.Channel;
import com.vcall.omnichannel.entity.Conversation.ConversationStatus;
import com.vcall.omnichannel.kafka.OmnichannelEventPublisher;
import com.vcall.omnichannel.repository.ConversationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageService messageService;
    private final OmnichannelEventPublisher eventPublisher;

    @Transactional
    public ConversationResponse updateConversation(UUID conversationId, ConversationUpdateRequest request) {
        Conversation conversation = findById(conversationId);
        if (request.getSubject() != null) conversation.setSubject(request.getSubject());
        if (request.getPriority() != null) conversation.setPriority(request.getPriority());
        conversation = conversationRepository.save(conversation);

        eventPublisher.publishConversationUpdated(conversation);
        return toResponse(conversation);
    }

    @Transactional
    public void deleteConversation(UUID conversationId) {
        Conversation conversation = findById(conversationId);
        conversation.setIsDeleted(true);
        conversationRepository.save(conversation);

        eventPublisher.publishConversationDeleted(conversation);
    }

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
    public Page<ConversationResponse> getByChannel(Channel channel, Pageable pageable) {
        return conversationRepository.findByChannel(channel, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ConversationResponse> getByCustomer(UUID customerId, Pageable pageable) {
        return conversationRepository.findByCustomerId(customerId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ConversationResponse> search(Channel channel, ConversationStatus status, UUID agentId, Pageable pageable) {
        if (channel != null) {
            return conversationRepository.findByChannel(channel, pageable)
                    .map(this::toResponse);
        }
        if (status != null) {
            return conversationRepository.findByStatus(status, pageable)
                    .map(this::toResponse);
        }
        if (agentId != null) {
            return conversationRepository.findByAgentId(agentId, pageable)
                    .map(this::toResponse);
        }
        return conversationRepository.findAll(pageable)
                .map(this::toResponse);
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
