package com.vcall.crm.repository;

import com.vcall.crm.entity.Opportunity;
import com.vcall.crm.entity.OpportunityStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, UUID> {

    List<Opportunity> findByLeadId(UUID leadId);

    List<Opportunity> findByStage(OpportunityStage stage);

    List<Opportunity> findByAssignedTo(UUID assignedTo);
}
