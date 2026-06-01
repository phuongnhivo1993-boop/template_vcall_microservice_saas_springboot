package com.vcall.crm.dto;

import com.vcall.crm.entity.LeadSource;
import com.vcall.crm.entity.LeadStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String email;

    private String phone;

    private String company;

    private String title;

    private LeadSource source;

    private LeadStatus status;

    private Integer score;

    private UUID assignedTo;

    private String notes;
}
