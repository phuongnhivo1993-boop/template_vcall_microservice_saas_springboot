package com.vcall.call.dto;

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
public class CallResponse {

    private UUID id;
    private String callId;
    private String callerNumber;
    private String calleeNumber;
    private String callerName;
    private String direction;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime answerTime;
    private LocalDateTime endTime;
    private Long duration;
    private UUID agentId;
    private Long queueId;
    private Long ivrFlowId;
    private UUID recordingId;

    private Integer satisfactionScore;
    private String satisfactionComment;
    private LocalDateTime satisfactionSurveyedAt;
    private Boolean satisfactionSurveySent;
}
