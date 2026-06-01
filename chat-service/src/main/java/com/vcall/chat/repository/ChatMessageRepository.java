package com.vcall.chat.repository;

import com.vcall.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    List<ChatMessage> findByConversationIdOrderBySentAtAsc(UUID conversationId);

    List<ChatMessage> findByConversationIdAndIsReadFalse(UUID conversationId);

    List<ChatMessage> findByConversationIdAndSentAtAfter(UUID conversationId, LocalDateTime sentAt);

    long countByConversationIdAndIsReadFalse(UUID conversationId);
}
