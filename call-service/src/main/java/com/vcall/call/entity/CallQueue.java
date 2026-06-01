package com.vcall.call.entity;

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
@Table(name = "call_queues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CallQueue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = true, nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "strategy", nullable = false, length = 20)
    private QueueStrategy strategy = QueueStrategy.RING_ALL;

    @Column(name = "max_wait_time")
    private Integer maxWaitTime;

    @Column(name = "max_queue_size")
    private Integer maxQueueSize;

    public enum QueueStrategy {
        RING_ALL, ROUND_ROBIN, LEAST_BUSY, RANDOM
    }
}
