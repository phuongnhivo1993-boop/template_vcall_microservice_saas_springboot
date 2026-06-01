package com.vcall.cdr.dto;

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
public class CdrRecordResponse {

    private UUID id;
    private String callId;
    private String callerNumber;
    private String calleeNumber;
    private String direction;
    private LocalDateTime startTime;
    private LocalDateTime answerTime;
    private LocalDateTime endTime;
    private Integer duration;
    private String status;
    private String hangupCause;
    private UUID agentId;
    private Long queueId;
    private UUID recordingId;
    private BigDecimal cost;
    private BigDecimal rate;
}
