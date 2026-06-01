package com.vcall.crm.dto;

import com.vcall.crm.entity.LeadSource;
import com.vcall.crm.entity.LeadStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadResponse {

    private UUID id;
    private String fullName;
    private String email;
    private String phone;
    private String company;
    private String title;
    private LeadSource source;
    private LeadStatus status;
    private Integer score;
    private UUID assignedTo;
    private LocalDateTime createdAt;
}
