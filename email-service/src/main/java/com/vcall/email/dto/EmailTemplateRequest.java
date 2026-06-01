package com.vcall.email.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String subject;

    private String bodyHtml;

    private String bodyText;

    private String variables;

    private String category;

    private Boolean isActive;
}
