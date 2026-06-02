package com.vcall.call.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SatisfactionRequest {
    @NotNull @Min(1) @Max(5)
    private Integer score;
    private String comment;
}
