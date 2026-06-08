package com.vcall.billing.repository;

import com.vcall.billing.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @EntityGraph(attributePaths = {"items"})
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    @EntityGraph(attributePaths = {"items"})
    List<Invoice> findBySubscriberIdOrderByIssueDateDesc(UUID subscriberId);

    @EntityGraph(attributePaths = {"items"})
    Page<Invoice> findBySubscriberIdOrderByIssueDateDesc(UUID subscriberId, Pageable pageable);

    @EntityGraph(attributePaths = {"items"})
    List<Invoice> findByStatus(Invoice.InvoiceStatus status);
    @EntityGraph(attributePaths = {"items"})
    Page<Invoice> findByStatus(Invoice.InvoiceStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"items"})
    List<Invoice> findByDueDateBeforeAndStatus(LocalDateTime dueDate, Invoice.InvoiceStatus status);
}
