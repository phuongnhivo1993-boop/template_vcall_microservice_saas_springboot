package com.vcall.ticket.repository;

import com.vcall.ticket.entity.SlaRule;
import com.vcall.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SlaRuleRepository extends JpaRepository<SlaRule, Long> {

    List<SlaRule> findByIsActiveTrue();

    List<SlaRule> findByPriorityAndCategory(Ticket.TicketPriority priority, String category);
}
