package com.vcall.survey.dto.response;

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
public class SurveyAnswerResponse {

    private UUID id;
    private UUID surveyId;
    private UUID questionId;
    private UUID customerId;
    private UUID callId;
    private UUID ticketId;
    private String answer;
    private Integer rating;
    private LocalDateTime submittedAt;
}
