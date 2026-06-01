package com.vcall.reporting.entity;

import com.vcall.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "dashboard_widgets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardWidget extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "widget_type", nullable = false, length = 30)
    private WidgetType widgetType;

    @Column(name = "data_query", columnDefinition = "TEXT")
    private String dataQuery;

    @Column(name = "config", columnDefinition = "TEXT")
    private String config;

    @Column(name = "position")
    private Integer position;

    @Column(name = "is_active")
    private boolean isActive = true;

    public enum WidgetType {
        LINE_CHART, BAR_CHART, PIE_CHART, TABLE, NUMBER_CARD
    }
}
