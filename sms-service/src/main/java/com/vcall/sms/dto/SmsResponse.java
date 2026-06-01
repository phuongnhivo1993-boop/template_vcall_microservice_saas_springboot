package com.vcall.sms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsResponse {

    private UUID id;
    private String messageId;
    private String fromNumber;
    private String toNumber;
    private String content;
    private String status;
    private LocalDateTime sentAt;
    private BigDecimal cost;
}
