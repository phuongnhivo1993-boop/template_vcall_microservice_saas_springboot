package com.vcall.survey.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyQuestionResponse {

    private UUID id;
    private UUID surveyId;
    private String questionText;
    private String questionType;
    private Integer orderIndex;
    private Boolean isRequired;
    private String options;
}
