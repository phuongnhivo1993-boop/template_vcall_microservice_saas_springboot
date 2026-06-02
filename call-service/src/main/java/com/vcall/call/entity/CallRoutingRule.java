package com.vcall.call.entity;

import com.vcall.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "call_routing_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_deleted = false")
public class CallRoutingRule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @Column(name = "condition", columnDefinition = "TEXT")
    private String condition;

    @Column(name = "destination", nullable = false, length = 50)
    private String destination;

    @Column(name = "destination_id")
    private Long destinationId;

    @Column(name = "time_profile", length = 255)
    private String timeProfile;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
