package com.vcall.chat.repository;

import com.vcall.chat.entity.ChatConversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, UUID> {

    @EntityGraph(attributePaths = {"messages"})
    Optional<ChatConversation> findById(UUID id);

    @EntityGraph(attributePaths = {"messages"})
    Optional<ChatConversation> findByConversationId(String conversationId);

    List<ChatConversation> findByCustomerId(UUID customerId);

    List<ChatConversation> findByAgentId(UUID agentId);
    Page<ChatConversation> findByAgentId(UUID agentId, Pageable pageable);

    List<ChatConversation> findByStatus(ChatConversation.Status status);
    Page<ChatConversation> findByStatus(ChatConversation.Status status, Pageable pageable);

    List<ChatConversation> findByStatusAndSource(ChatConversation.Status status, ChatConversation.Source source);

    long countByStatus(ChatConversation.Status status);
}
