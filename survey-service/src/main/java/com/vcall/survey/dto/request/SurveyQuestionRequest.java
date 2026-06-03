package com.vcall.survey.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyQuestionRequest {

    @NotNull(message = "Survey ID is required")
    private UUID surveyId;

    @NotBlank(message = "Question text is required")
    private String questionText;

    @NotBlank(message = "Question type is required")
    private String questionType;

    @NotNull(message = "Order index is required")
    private Integer orderIndex;

    private Boolean isRequired = false;

    private String options;
}
