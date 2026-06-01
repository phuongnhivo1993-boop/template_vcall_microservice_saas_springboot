package com.vcall.ticket.repository;

import com.vcall.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID>, JpaSpecificationExecutor<Ticket> {

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    List<Ticket> findByCustomerId(UUID customerId);

    List<Ticket> findByAssignedTo(UUID assignedTo);

    List<Ticket> findByStatus(Ticket.TicketStatus status);

    List<Ticket> findByPriority(Ticket.TicketPriority priority);

    List<Ticket> findBySource(Ticket.TicketSource source);

    List<Ticket> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByStatus(Ticket.TicketStatus status);

    long countByAssignedToAndStatus(UUID assignedTo, Ticket.TicketStatus status);
}
