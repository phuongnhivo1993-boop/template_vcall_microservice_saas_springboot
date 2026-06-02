package com.vcall.call.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CallEvaluationRequest {
    @NotNull @Min(0) @Max(100)
    private Integer score;
    private Integer greetingScore;
    private Integer knowledgeScore;
    private Integer resolutionScore;
    private Integer communicationScore;
    private Integer empathyScore;
    private Integer complianceScore;
    private String comments;
    private String strengths;
    private String improvements;
}
