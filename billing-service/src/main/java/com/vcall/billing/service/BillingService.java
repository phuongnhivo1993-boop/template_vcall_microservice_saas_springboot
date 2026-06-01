package com.vcall.billing.service;

import com.vcall.billing.dto.InvoiceResponse;
import com.vcall.billing.entity.Invoice;
import com.vcall.billing.entity.InvoiceItem;
import com.vcall.billing.entity.UsageRecord;
import com.vcall.billing.kafka.BillingEventPublisher;
import com.vcall.billing.repository.InvoiceRepository;
import com.vcall.billing.repository.UsageRecordRepository;
import com.vcall.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

    private final InvoiceRepository invoiceRepository;
    private final UsageRecordRepository usageRecordRepository;
    private final BillingEventPublisher eventPublisher;

    @Transactional
    public InvoiceResponse generateInvoice(UUID subscriberId, LocalDateTime startDate, LocalDateTime endDate) {
        List<UsageRecord> usageRecords = usageRecordRepository.findAll().stream()
                .filter(r -> r.getSubscriberId().equals(subscriberId)
                        && !r.getRecordedAt().isBefore(startDate)
                        && !r.getRecordedAt().isAfter(endDate))
                .collect(Collectors.toList());

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(generateInvoiceNumber(subscriberId));
        invoice.setSubscriberId(subscriberId);
        invoice.setStatus(Invoice.InvoiceStatus.DRAFT);
        invoice.setIssueDate(LocalDateTime.now());
        invoice.setDueDate(LocalDateTime.now().plusDays(30));
        invoice.setCurrency("USD");
        invoice.setSubtotal(BigDecimal.ZERO);

        for (UsageRecord record : usageRecords) {
            InvoiceItem item = new InvoiceItem();
            item.setInvoice(invoice);
            item.setDescription(record.getUsageType().name() + " - " + record.getRecordedAt().toLocalDate().toString());
            item.setQuantity(record.getQuantity());
            item.setUnitPrice(record.getUnitPrice());
            item.setTotalPrice(record.getTotalCost() != null ? record.getTotalCost() : BigDecimal.ZERO);
            item.setUsageType(record.getUsageType());
            invoice.getItems().add(item);
            invoice.setSubtotal(invoice.getSubtotal().add(item.getTotalPrice()));
        }

        BigDecimal taxRate = new BigDecimal("0.10");
        invoice.setTax(invoice.getSubtotal().multiply(taxRate).setScale(2, RoundingMode.HALF_UP));
        invoice.setDiscount(BigDecimal.ZERO);
        invoice.setTotal(invoice.getSubtotal().add(invoice.getTax()).subtract(invoice.getDiscount()));
        invoice = invoiceRepository.save(invoice);

        eventPublisher.publishInvoiceCreated(invoice);
        return toResponse(invoice);
    }

    @Transactional
    public InvoiceResponse processPayment(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));
        invoice.setStatus(Invoice.InvoiceStatus.PAID);
        invoice.setPaidAt(LocalDateTime.now());
        invoice = invoiceRepository.save(invoice);

        eventPublisher.publishPaymentCompleted(invoice);
        return toResponse(invoice);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
        return toResponse(invoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoiceHistory(UUID subscriberId) {
        return invoiceRepository.findBySubscriberIdOrderByIssueDateDesc(subscriberId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByStatus(Invoice.InvoiceStatus status) {
        return invoiceRepository.findByStatus(status).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getOverdueInvoices() {
        return invoiceRepository.findByDueDateBeforeAndStatus(LocalDateTime.now(), Invoice.InvoiceStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateUsageCost(UUID subscriberId, LocalDateTime startDate, LocalDateTime endDate) {
        List<UsageRecord> records = usageRecordRepository.findAll().stream()
                .filter(r -> r.getSubscriberId().equals(subscriberId)
                        && !r.getRecordedAt().isBefore(startDate)
                        && !r.getRecordedAt().isAfter(endDate))
                .collect(Collectors.toList());
        return records.stream()
                .map(r -> r.getTotalCost() != null ? r.getTotalCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generateInvoiceNumber(UUID subscriberId) {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String shortId = subscriberId.toString().substring(0, 8).toUpperCase();
        return "INV-" + datePart + "-" + shortId;
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .subscriberId(invoice.getSubscriberId().toString())
                .status(invoice.getStatus().name())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .paidAt(invoice.getPaidAt())
                .subtotal(invoice.getSubtotal())
                .tax(invoice.getTax())
                .total(invoice.getTotal())
                .items(invoice.getItems())
                .build();
    }
}
