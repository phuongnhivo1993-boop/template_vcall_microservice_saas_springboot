package com.vcall.chat.service;

import com.vcall.chat.dto.ChatAssignRequest;
import com.vcall.chat.dto.ChatConversationRequest;
import com.vcall.chat.dto.ChatConversationResponse;
import com.vcall.chat.entity.ChatConversation;
import com.vcall.chat.entity.ChatConversation.Source;
import com.vcall.chat.entity.ChatConversation.Status;
import com.vcall.chat.entity.ChatMessage;
import com.vcall.chat.kafka.ChatEventPublisher;
import com.vcall.chat.repository.ChatConversationRepository;
import com.vcall.chat.repository.ChatMessageRepository;
import com.vcall.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final ChatEventPublisher eventPublisher;

    @Transactional
    public ChatConversationResponse createConversation(ChatConversationRequest request) {
        ChatConversation conversation = new ChatConversation();
        conversation.setConversationId(UUID.randomUUID().toString());
        conversation.setCustomerId(request.getCustomerId());
        conversation.setSource(request.getSource());
        conversation.setStatus(Status.WAITING);
        conversation.setCustomerName(request.getCustomerName());
        conversation.setCustomerEmail(request.getCustomerEmail());
        conversation.setPageUrl(request.getPageUrl());
        conversation.setUserAgent(request.getUserAgent());
        conversation = conversationRepository.save(conversation);

        eventPublisher.publishChatStarted(conversation);
        return toResponse(conversation);
    }

    @Transactional
    public ChatConversationResponse assignAgent(UUID conversationId, ChatAssignRequest request) {
        ChatConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));
        conversation.setAgentId(request.getAgentId());
        conversation.setAssignedAt(LocalDateTime.now());
        conversation.setStatus(Status.ACTIVE);
        conversation = conversationRepository.save(conversation);

        eventPublisher.publishChatStarted(conversation);
        return toResponse(conversation);
    }

    @Transactional
    public ChatConversationResponse closeConversation(UUID conversationId) {
        ChatConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));
        conversation.setStatus(Status.CLOSED);
        conversation.setClosedAt(LocalDateTime.now());
        conversation = conversationRepository.save(conversation);

        eventPublisher.publishChatClosed(conversation);
        return toResponse(conversation);
    }

    @Transactional(readOnly = true)
    public List<ChatConversationResponse> getActiveConversations() {
        return conversationRepository.findByStatus(Status.ACTIVE).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChatConversationResponse getConversationHistory(UUID conversationId) {
        ChatConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));
        return toResponse(conversation);
    }

    @Transactional(readOnly = true)
    public List<ChatConversationResponse> getByStatus(Status status) {
        return conversationRepository.findByStatus(status).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatConversationResponse> getByAgentId(UUID agentId) {
        return conversationRepository.findByAgentId(agentId).stream()
                .map(this::toResponse)
                .toList();
    }

    private ChatConversationResponse toResponse(ChatConversation conversation) {
        List<ChatMessage> messages = conversation.getMessages();
        ChatMessage lastMsg = messages.isEmpty() ? null : messages.get(messages.size() - 1);

        return ChatConversationResponse.builder()
                .id(conversation.getId())
                .conversationId(conversation.getConversationId())
                .customerId(conversation.getCustomerId())
                .agentId(conversation.getAgentId())
                .source(conversation.getSource().name())
                .status(conversation.getStatus().name())
                .assignedAt(conversation.getAssignedAt())
                .closedAt(conversation.getClosedAt())
                .customerName(conversation.getCustomerName())
                .messageCount(messages.size())
                .lastMessage(lastMsg != null ? lastMsg.getContent() : null)
                .createdAt(conversation.getCreatedAt())
                .build();
    }
}
