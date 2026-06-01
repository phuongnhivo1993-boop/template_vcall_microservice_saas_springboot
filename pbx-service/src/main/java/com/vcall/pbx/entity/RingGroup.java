package com.vcall.pbx.entity;

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
@Table(name = "ring_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RingGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "strategy", nullable = false, length = 20)
    private RingStrategy strategy;

    @Column(name = "ring_timeout")
    private Integer ringTimeout = 30;

    @Column(name = "ringback_tone", length = 100)
    private String ringbackTone;

    public enum RingStrategy {
        SEQUENTIAL, SIMULTANEOUS
    }
}
