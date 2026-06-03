package com.vcall.survey.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyStatsResponse {

    private long totalResponses;
    private double averageRating;
    private long csatCount;
    private double csatScore;
    private long npsCount;
    private Integer npsScore;
    private long promoterCount;
    private long passiveCount;
    private long detractorCount;
}
