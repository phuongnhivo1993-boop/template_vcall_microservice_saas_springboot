package com.vcall.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardWidgetResponse {

    private Long id;
    private String name;
    private String widgetType;
    private String config;
    private Integer position;
}
