package com.vcall.recording.entity;

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
@Table(name = "retention_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RetentionPolicy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "retention_days", nullable = false)
    private Integer retentionDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private RetentionAction action = RetentionAction.DELETE;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    public enum RetentionAction {
        DELETE, ARCHIVE, COMPRESS
    }
}
