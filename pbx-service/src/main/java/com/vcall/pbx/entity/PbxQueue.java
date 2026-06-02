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
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "pbx_queues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_deleted = false")
public class PbxQueue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "strategy", nullable = false, length = 20)
    private QueueStrategy strategy;

    @Column(name = "max_wait_time")
    private Integer maxWaitTime = 300;

    @Column(name = "max_queue_size")
    private Integer maxQueueSize = 50;

    @Enumerated(EnumType.STRING)
    @Column(name = "timeout_action", length = 20)
    private TimeoutAction timeoutAction;

    @Column(name = "timeout_destination", length = 100)
    private String timeoutDestination;

    public enum QueueStrategy {
        FIFO, ROUND_ROBIN, RING_ALL
    }

    public enum TimeoutAction {
        VOICEMAIL, TRANSFER, HANGUP
    }
}
