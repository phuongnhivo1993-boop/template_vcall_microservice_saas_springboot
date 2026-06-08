package com.vcall.chat.service;

import com.vcall.chat.dto.AttachmentResponse;
import com.vcall.chat.dto.ChatMessageRequest;
import com.vcall.chat.dto.ChatMessageResponse;
import com.vcall.chat.entity.Attachment;
import com.vcall.chat.entity.ChatConversation;
import com.vcall.chat.entity.ChatMessage;
import com.vcall.chat.kafka.ChatEventPublisher;
import com.vcall.chat.repository.AttachmentRepository;
import com.vcall.chat.repository.ChatConversationRepository;
import com.vcall.chat.repository.ChatMessageRepository;
import com.vcall.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository messageRepository;
    private final ChatConversationRepository conversationRepository;
    private final AttachmentRepository attachmentRepository;
    private final ChatAttachmentStorageService attachmentStorageService;
    private final ChatEventPublisher eventPublisher;

    @Transactional
    public ChatMessageResponse sendMessage(UUID conversationId, ChatMessageRequest request) {
        ChatConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));

        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSenderType(request.getSenderType());
        message.setSenderId(request.getSenderId());
        message.setContent(request.getContent());
        message.setContentType(request.getContentType());
        message.setSentAt(LocalDateTime.now());
        message.setRead(false);
        message = messageRepository.save(message);

        eventPublisher.publishChatMessageSent(conversation, message);
        return toResponse(message);
    }

    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getMessages(UUID conversationId, Pageable pageable) {
        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public void markAsRead(UUID conversationId) {
        List<ChatMessage> unreadMessages = messageRepository.findByConversationIdAndIsReadFalse(conversationId);
        unreadMessages.forEach(msg -> msg.setRead(true));
        messageRepository.saveAll(unreadMessages);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID conversationId) {
        return messageRepository.countByConversationIdAndIsReadFalse(conversationId);
    }

    @Transactional
    public AttachmentResponse addAttachment(UUID conversationId, UUID messageId, MultipartFile file) {
        conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));

        ChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));

        String fileUrl = attachmentStorageService.storeFile(messageId, file);

        Attachment attachment = new Attachment();
        attachment.setMessage(message);
        attachment.setFileName(file.getOriginalFilename());
        attachment.setFileUrl(fileUrl);
        attachment.setFileSize(file.getSize());
        attachment.setMimeType(file.getContentType());
        attachment = attachmentRepository.save(attachment);

        message.setAttachmentUrl(fileUrl);
        message.setContentType(ChatMessage.ContentType.FILE);
        messageRepository.save(message);

        return AttachmentResponse.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .fileUrl(attachment.getFileUrl())
                .fileSize(attachment.getFileSize())
                .mimeType(attachment.getMimeType())
                .build();
    }

    private ChatMessageResponse toResponse(ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderType(message.getSenderType().name())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .contentType(message.getContentType().name())
                .sentAt(message.getSentAt())
                .isRead(message.isRead())
                .build();
    }
}
