package com.vcall.billing.dto;

import com.vcall.billing.entity.InvoiceItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {

    private Long id;
    private String invoiceNumber;
    private String subscriberId;
    private String status;
    private LocalDateTime issueDate;
    private LocalDateTime dueDate;
    private LocalDateTime paidAt;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;
    private List<InvoiceItem> items;
}
