package com.vcall.cdr.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CdrImportRequest {

    @NotBlank
    private String fileUrl;

    @NotBlank
    private String fileFormat;
}
