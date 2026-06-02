package com.vcall.call.repository;

import com.vcall.call.entity.CallRoutingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CallRoutingRuleRepository extends JpaRepository<CallRoutingRule, Long>, JpaSpecificationExecutor<CallRoutingRule> {

    List<CallRoutingRule> findByIsActiveTrueOrderByPriority();
}
