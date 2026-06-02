package com.vcall.omnichannel.repository;

import com.vcall.omnichannel.entity.Conversation;
import com.vcall.omnichannel.entity.Conversation.Channel;
import com.vcall.omnichannel.entity.Conversation.ConversationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID>, JpaSpecificationExecutor<Conversation> {

    List<Conversation> findByChannel(Channel channel);

    Page<Conversation> findByChannel(Channel channel, Pageable pageable);

    List<Conversation> findByCustomerId(UUID customerId);

    Page<Conversation> findByCustomerId(UUID customerId, Pageable pageable);

    List<Conversation> findByAgentId(UUID agentId);

    Page<Conversation> findByAgentId(UUID agentId, Pageable pageable);

    List<Conversation> findByStatus(ConversationStatus status);

    Page<Conversation> findByStatus(ConversationStatus status, Pageable pageable);

    Optional<Conversation> findByChannelAndExternalId(Channel channel, String externalId);
}
