package com.vcall.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDataResponse {

    private Long widgetId;
    private String widgetName;
    private String widgetType;
    private Map<String, Object> data;
    private String config;
}
