package com.vcall.agent.repository;

import com.vcall.agent.entity.AgentGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentGroupRepository extends JpaRepository<AgentGroup, Long>, JpaSpecificationExecutor<AgentGroup> {

    Optional<AgentGroup> findByName(String name);
}
