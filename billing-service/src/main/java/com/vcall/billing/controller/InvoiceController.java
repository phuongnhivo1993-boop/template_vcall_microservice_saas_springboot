package com.vcall.billing.controller;

import com.vcall.billing.dto.InvoiceResponse;
import com.vcall.billing.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/billing/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final BillingService billingService;

    @PostMapping("/generate")
    public ResponseEntity<InvoiceResponse> generateInvoice(
            @RequestParam UUID subscriberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(billingService.generateInvoice(subscriberId, startDate, endDate));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.getInvoice(id));
    }

    @GetMapping("/subscriber/{subscriberId}")
    public ResponseEntity<Page<InvoiceResponse>> getSubscriberInvoices(@PathVariable UUID subscriberId,
                                                                        Pageable pageable) {
        return ResponseEntity.ok(billingService.getInvoiceHistory(subscriberId, pageable));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<InvoiceResponse> payInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.processPayment(id));
    }
}
