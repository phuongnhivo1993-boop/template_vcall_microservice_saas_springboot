package com.vcall.cdr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallVolumeResponse {

    private String period;
    private Long volume;
    private Long answered;
    private Long missed;
    private Double avgDuration;
}
