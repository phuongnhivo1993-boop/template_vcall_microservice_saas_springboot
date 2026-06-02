package com.vcall.ticket.repository;

import com.vcall.ticket.entity.Ticket;
import com.vcall.ticket.entity.Ticket.TicketPriority;
import com.vcall.ticket.entity.Ticket.TicketSource;
import com.vcall.ticket.entity.Ticket.TicketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TicketRepositoryTests {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EntityManager entityManager;

    private Ticket ticket1;
    private Ticket ticket2;

    @BeforeEach
    void setUp() {
        ticket1 = new Ticket();
        ticket1.setTicketNumber("TKT24010100001");
        ticket1.setTitle("Network Issue");
        ticket1.setDescription("Cannot connect to VPN");
        ticket1.setCustomerId(UUID.randomUUID());
        ticket1.setSource(TicketSource.PORTAL);
        ticket1.setCategory("TECHNICAL");
        ticket1.setPriority(TicketPriority.HIGH);
        ticket1.setStatus(TicketStatus.OPEN);
        ticket1.setIsDeleted(false);
        ticketRepository.save(ticket1);

        ticket2 = new Ticket();
        ticket2.setTicketNumber("TKT24010100002");
        ticket2.setTitle("Billing Inquiry");
        ticket2.setDescription("Invoice discrepancy");
        ticket2.setCustomerId(UUID.randomUUID());
        ticket2.setSource(TicketSource.EMAIL);
        ticket2.setCategory("BILLING");
        ticket2.setPriority(TicketPriority.MEDIUM);
        ticket2.setStatus(TicketStatus.IN_PROGRESS);
        ticket2.setAssignedTo(UUID.randomUUID());
        ticket2.setIsDeleted(false);
        ticketRepository.save(ticket2);

        entityManager.flush();
    }

    @Test
    void testFindAll() {
        List<Ticket> tickets = ticketRepository.findAll();
        assertThat(tickets).hasSize(2);
    }

    @Test
    void testFindById() {
        Optional<Ticket> found = ticketRepository.findById(ticket1.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Network Issue");
    }

    @Test
    void testFindByTicketNumber() {
        Optional<Ticket> found = ticketRepository.findByTicketNumber("TKT24010100001");
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Network Issue");
    }

    @Test
    void testFindByCustomerId() {
        List<Ticket> found = ticketRepository.findByCustomerId(ticket1.getCustomerId());
        assertThat(found).hasSize(1);
    }

    @Test
    void testFindByStatus() {
        List<Ticket> openTickets = ticketRepository.findByStatus(TicketStatus.OPEN);
        assertThat(openTickets).hasSize(1);
        assertThat(openTickets.get(0).getTitle()).isEqualTo("Network Issue");
    }

    @Test
    void testFindByPriority() {
        List<Ticket> highPriority = ticketRepository.findByPriority(TicketPriority.HIGH);
        assertThat(highPriority).hasSize(1);
    }

    @Test
    void testFindByAssignedTo() {
        List<Ticket> assigned = ticketRepository.findByAssignedTo(ticket2.getAssignedTo());
        assertThat(assigned).hasSize(1);
    }

    @Test
    void testFindBySource() {
        List<Ticket> portalTickets = ticketRepository.findBySource(TicketSource.PORTAL);
        assertThat(portalTickets).hasSize(1);
    }

    @Test
    void testFindByCreatedAtBetween() {
        LocalDateTime start = LocalDateTime.now().minusMinutes(5);
        LocalDateTime end = LocalDateTime.now().plusMinutes(5);
        List<Ticket> found = ticketRepository.findByCreatedAtBetween(start, end);
        assertThat(found).hasSize(2);
    }

    @Test
    void testCountByStatus() {
        long count = ticketRepository.countByStatus(TicketStatus.OPEN);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void testCountByAssignedToAndStatus() {
        long count = ticketRepository.countByAssignedToAndStatus(ticket2.getAssignedTo(), TicketStatus.IN_PROGRESS);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void testPagination() {
        Page<Ticket> page1 = ticketRepository.findAll(PageRequest.of(0, 1, Sort.by("title").ascending()));
        assertThat(page1.getContent()).hasSize(1);
        assertThat(page1.getTotalPages()).isEqualTo(2);
        assertThat(page1.getTotalElements()).isEqualTo(2);
    }

    @Test
    void testSorting() {
        Page<Ticket> result = ticketRepository.findAll(PageRequest.of(0, 10, Sort.by("title").descending()));
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Network Issue");
    }

    @Test
    void testSpecificationWithKeyword() {
        Page<Ticket> result = ticketRepository.findAll((root, query, cb) -> {
            String pattern = "%network%";
            return cb.like(cb.lower(root.get("title")), pattern);
        }, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Network Issue");
    }

    @Test
    void testSoftDelete() {
        Ticket ticket = ticketRepository.findById(ticket1.getId()).orElseThrow();
        ticket.setIsDeleted(true);
        ticketRepository.save(ticket);
        entityManager.flush();

        Optional<Ticket> found = ticketRepository.findById(ticket1.getId());
        assertThat(found).isEmpty();
    }
}
