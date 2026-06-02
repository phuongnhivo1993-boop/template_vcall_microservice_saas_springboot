package com.vcall.ticket.repository;

import com.vcall.ticket.entity.TicketComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TicketCommentRepository extends JpaRepository<TicketComment, Long>, JpaSpecificationExecutor<TicketComment> {

    List<TicketComment> findByTicketIdOrderByCreatedAtAsc(UUID ticketId);

    Page<TicketComment> findByTicketId(UUID ticketId, Pageable pageable);

    Page<TicketComment> findByTicketIdAndIsInternal(UUID ticketId, boolean isInternal, Pageable pageable);

    List<TicketComment> findByAuthorId(UUID authorId);
}
