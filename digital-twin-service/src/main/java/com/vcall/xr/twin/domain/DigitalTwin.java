package com.vcall.xr.twin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "xr_digital_twin")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DigitalTwin {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "type", nullable = false, length = 100)
    private String type;

    @Column(name = "bim_asset_id")
    private UUID bimAssetId;

    @Column(name = "scene_id")
    private UUID sceneId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "iot_endpoints", columnDefinition = "jsonb")
    private String iotEndpoints;

    @Column(name = "sync_interval_seconds")
    private Integer syncIntervalSeconds;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "floors", columnDefinition = "jsonb")
    private String floors;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rooms", columnDefinition = "jsonb")
    private String rooms;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted;
}
