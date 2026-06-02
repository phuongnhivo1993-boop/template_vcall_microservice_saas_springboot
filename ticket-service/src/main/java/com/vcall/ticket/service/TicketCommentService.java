package com.vcall.ticket.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.ticket.dto.TicketCommentRequest;
import com.vcall.ticket.dto.TicketCommentResponse;
import com.vcall.ticket.entity.Ticket;
import com.vcall.ticket.entity.TicketComment;
import com.vcall.ticket.entity.TicketComment.AuthorType;
import com.vcall.ticket.repository.TicketCommentRepository;
import com.vcall.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class TicketCommentService {

    private final TicketCommentRepository commentRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public TicketCommentResponse addComment(UUID ticketId, TicketCommentRequest request, UUID authorId, AuthorType authorType) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));

        if (ticket.getFirstResponseAt() == null && authorType == AuthorType.AGENT) {
            ticket.setFirstResponseAt(LocalDateTime.now());
            ticketRepository.save(ticket);
        }

        TicketComment comment = new TicketComment();
        comment.setTicket(ticket);
        comment.setContent(request.getContent());
        comment.setAuthorId(authorId);
        comment.setAuthorType(authorType);
        comment.setIsInternal(request.getIsInternal() != null && request.getIsInternal());
        comment = commentRepository.save(comment);

        return toResponse(comment);
    }

    @Transactional(readOnly = true)
    public Page<TicketCommentResponse> getComments(UUID ticketId, Pageable pageable) {
        return commentRepository.findByTicketId(ticketId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<TicketCommentResponse> getInternalComments(UUID ticketId, Pageable pageable) {
        return commentRepository.findByTicketIdAndIsInternal(ticketId, true, pageable).map(this::toResponse);
    }

    private TicketCommentResponse toResponse(TicketComment comment) {
        return TicketCommentResponse.builder()
                .id(comment.getId())
                .ticketId(comment.getTicket().getId())
                .content(comment.getContent())
                .authorId(comment.getAuthorId())
                .authorType(comment.getAuthorType().name())
                .isInternal(comment.getIsInternal())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
