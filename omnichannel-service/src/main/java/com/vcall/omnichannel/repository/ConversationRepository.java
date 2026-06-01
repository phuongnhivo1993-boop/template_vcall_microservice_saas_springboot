package com.vcall.omnichannel.repository;

import com.vcall.omnichannel.entity.Conversation;
import com.vcall.omnichannel.entity.Conversation.Channel;
import com.vcall.omnichannel.entity.Conversation.ConversationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID>, JpaSpecificationExecutor<Conversation> {

    List<Conversation> findByChannel(Channel channel);

    List<Conversation> findByCustomerId(UUID customerId);

    List<Conversation> findByAgentId(UUID agentId);

    List<Conversation> findByStatus(ConversationStatus status);

    Optional<Conversation> findByChannelAndExternalId(Channel channel, String externalId);
}
