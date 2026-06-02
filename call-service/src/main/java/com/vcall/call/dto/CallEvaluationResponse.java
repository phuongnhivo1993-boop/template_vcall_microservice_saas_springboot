package com.vcall.call.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CallEvaluationResponse {
    private UUID id;
    private UUID callId;
    private UUID evaluatorId;
    private String evaluatorName;
    private Integer score;
    private Integer maxScore;
    private Integer greetingScore;
    private Integer knowledgeScore;
    private Integer resolutionScore;
    private Integer communicationScore;
    private Integer empathyScore;
    private Integer complianceScore;
    private String comments;
    private String strengths;
    private String improvements;
    private String status;
    private LocalDateTime evaluationDate;
    private LocalDateTime createdAt;
}
