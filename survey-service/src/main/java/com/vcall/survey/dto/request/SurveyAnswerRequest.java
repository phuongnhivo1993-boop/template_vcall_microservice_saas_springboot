package com.vcall.survey.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyAnswerRequest {

    @NotNull(message = "Survey ID is required")
    private UUID surveyId;

    @NotNull(message = "Question ID is required")
    private UUID questionId;

    private UUID customerId;

    private UUID callId;

    private UUID ticketId;

    private String answer;

    private Integer rating;
}
