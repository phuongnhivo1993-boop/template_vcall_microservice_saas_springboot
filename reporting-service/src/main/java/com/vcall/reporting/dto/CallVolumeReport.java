package com.vcall.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallVolumeReport {

    private String period;
    private Long totalCalls;
    private Long answered;
    private Long missed;
    private Long failed;
    private Double avgDuration;
    private Double answerRate;
}
