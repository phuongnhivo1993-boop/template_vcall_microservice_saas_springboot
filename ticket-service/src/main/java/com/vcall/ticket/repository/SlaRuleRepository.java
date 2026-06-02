package com.vcall.ticket.repository;

import com.vcall.ticket.entity.SlaRule;
import com.vcall.ticket.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SlaRuleRepository extends JpaRepository<SlaRule, Long>, JpaSpecificationExecutor<SlaRule> {

    List<SlaRule> findByIsActiveTrue();

    Page<SlaRule> findByIsActiveTrue(Pageable pageable);

    List<SlaRule> findByPriorityAndCategory(Ticket.TicketPriority priority, String category);
}
