package com.vcall.omnichannel.repository;

import com.vcall.omnichannel.entity.Conversation.Channel;
import com.vcall.omnichannel.entity.OmnichannelRoutingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OmnichannelRoutingRuleRepository extends JpaRepository<OmnichannelRoutingRule, Long> {

    List<OmnichannelRoutingRule> findByChannelAndIsActiveTrueOrderByPriority(Channel channel);

    Optional<OmnichannelRoutingRule> findByName(String name);
}
