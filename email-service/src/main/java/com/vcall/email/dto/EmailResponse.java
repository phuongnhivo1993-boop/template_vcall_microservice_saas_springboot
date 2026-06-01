package com.vcall.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailResponse {

    private UUID id;
    private String messageId;
    private String fromAddress;
    private String toAddresses;
    private String subject;
    private String bodyText;
    private String direction;
    private String status;
    private LocalDateTime sentAt;
    private LocalDateTime receivedAt;
}
