package com.vcall.omnichannel.service;

import com.vcall.omnichannel.dto.request.MessageRequest;
import com.vcall.omnichannel.dto.response.MessageResponse;
import com.vcall.omnichannel.entity.Conversation;
import com.vcall.omnichannel.entity.Message;
import com.vcall.omnichannel.entity.Message.ContentType;
import com.vcall.omnichannel.kafka.OmnichannelEventPublisher;
import com.vcall.omnichannel.repository.ConversationRepository;
import com.vcall.omnichannel.repository.MessageRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final OmnichannelEventPublisher eventPublisher;

    @Transactional
    public MessageResponse sendMessage(UUID conversationId, MessageRequest request) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found with id: " + conversationId));

        Message message = new Message();
        message.setConversation(conversation);
        message.setSenderType(request.getSenderType());
        message.setSenderId(request.getSenderId());
        message.setContent(request.getContent());
        message.setContentType(request.getContentType() != null ? request.getContentType() : ContentType.TEXT);
        message.setAttachmentUrl(request.getAttachmentUrl());
        message.setSentAt(LocalDateTime.now());
        message.setIsRead(false);

        message = messageRepository.save(message);

        eventPublisher.publishMessageSent(message);

        return toResponse(message);
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessages(UUID conversationId, Pageable pageable) {
        return messageRepository.findByConversationIdOrderBySentAt(conversationId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessagesAfter(UUID conversationId, LocalDateTime after, Pageable pageable) {
        return messageRepository.findByConversationIdAndSentAtAfter(conversationId, after, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public void markAsRead(UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found with id: " + messageId));
        message.setIsRead(true);
        messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public long getMessageCount(UUID conversationId) {
        return messageRepository.findByConversationIdOrderBySentAt(conversationId).size();
    }

    @Transactional(readOnly = true)
    public String getLastMessageContent(UUID conversationId) {
        List<Message> messages = messageRepository.findByConversationIdOrderBySentAt(conversationId);
        if (messages.isEmpty()) {
            return null;
        }
        return messages.get(messages.size() - 1).getContent();
    }

    private MessageResponse toResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderType(message.getSenderType())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .contentType(message.getContentType())
                .attachmentUrl(message.getAttachmentUrl())
                .sentAt(message.getSentAt())
                .isRead(message.getIsRead())
                .build();
    }
}
