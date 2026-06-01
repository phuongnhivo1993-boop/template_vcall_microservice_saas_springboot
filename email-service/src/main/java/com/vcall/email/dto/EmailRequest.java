package com.vcall.email.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

    @NotBlank
    private List<String> toAddresses;

    private List<String> ccAddresses;

    private List<String> bccAddresses;

    @NotBlank
    private String subject;

    private String bodyHtml;

    private String bodyText;

    private Long templateId;

    private List<String> attachments;
}
