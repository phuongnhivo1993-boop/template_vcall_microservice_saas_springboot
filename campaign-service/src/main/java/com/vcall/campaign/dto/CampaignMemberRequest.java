package com.vcall.campaign.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignMemberRequest {

    @Size(max = 255)
    private String contactName;

    @Size(max = 20)
    private String contactPhone;

    @Size(max = 255)
    private String contactEmail;

    private String contactData;

    private Integer priority = 0;
}
