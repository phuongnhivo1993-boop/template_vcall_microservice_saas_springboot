package com.vcall.crm.repository;

import com.vcall.crm.entity.Opportunity;
import com.vcall.crm.entity.OpportunityStage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, UUID> {

    List<Opportunity> findByLeadId(UUID leadId);

    Page<Opportunity> findByLeadId(UUID leadId, Pageable pageable);

    List<Opportunity> findByStage(OpportunityStage stage);

    Page<Opportunity> findByStage(OpportunityStage stage, Pageable pageable);

    List<Opportunity> findByAssignedTo(UUID assignedTo);
}
