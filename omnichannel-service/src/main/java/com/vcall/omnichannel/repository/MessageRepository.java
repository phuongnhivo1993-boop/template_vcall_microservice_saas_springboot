package com.vcall.omnichannel.repository;

import com.vcall.omnichannel.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findByConversationIdOrderBySentAt(UUID conversationId);

    List<Message> findByConversationIdAndSentAtAfter(UUID conversationId, LocalDateTime sentAt);
}
