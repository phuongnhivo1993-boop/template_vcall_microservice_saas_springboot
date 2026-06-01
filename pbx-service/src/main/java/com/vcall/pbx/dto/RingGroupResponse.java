package com.vcall.pbx.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RingGroupResponse {

    private Long id;
    private String name;
    private String description;
    private String strategy;
    private Integer ringTimeout;
    private int memberCount;
}
