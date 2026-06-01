package com.vcall.billing.repository;

import com.vcall.billing.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findBySubscriberIdOrderByIssueDateDesc(UUID subscriberId);

    List<Invoice> findByStatus(Invoice.InvoiceStatus status);

    List<Invoice> findByDueDateBeforeAndStatus(LocalDateTime dueDate, Invoice.InvoiceStatus status);
}
