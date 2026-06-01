package com.vcall.agent.repository;

import com.vcall.agent.entity.AgentGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentGroupRepository extends JpaRepository<AgentGroup, Long> {

    Optional<AgentGroup> findByName(String name);
}
