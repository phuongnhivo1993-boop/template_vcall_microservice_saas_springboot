package com.vcall.ticket.repository;

import com.vcall.ticket.entity.SlaBreach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SlaBreachRepository extends JpaRepository<SlaBreach, Long> {

    List<SlaBreach> findByTicketId(UUID ticketId);

    List<SlaBreach> findByBreachedAtBetween(LocalDateTime start, LocalDateTime end);

    List<SlaBreach> findByNotifiedFalse();
}
