package com.vcall.omnichannel.repository;

import com.vcall.omnichannel.entity.Conversation.Channel;
import com.vcall.omnichannel.entity.OmnichannelRoutingRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OmnichannelRoutingRuleRepository extends JpaRepository<OmnichannelRoutingRule, Long> {

    List<OmnichannelRoutingRule> findByChannelAndIsActiveTrueOrderByPriority(Channel channel);

    Page<OmnichannelRoutingRule> findByChannelAndIsActiveTrueOrderByPriority(Channel channel, Pageable pageable);

    Optional<OmnichannelRoutingRule> findByName(String name);
}
