package com.vcall.cdr.entity;

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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cdr_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CdrRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "call_id", unique = true, nullable = false, length = 255)
    private String callId;

    @Column(name = "caller_number", length = 50)
    private String callerNumber;

    @Column(name = "callee_number", length = 50)
    private String calleeNumber;

    @Column(name = "caller_name", length = 255)
    private String callerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 20)
    private Direction direction;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "answer_time")
    private LocalDateTime answerTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "wait_duration")
    private Integer waitDuration;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    @Column(name = "hangup_cause", length = 100)
    private String hangupCause;

    @Column(name = "agent_id", columnDefinition = "UUID")
    private UUID agentId;

    @Column(name = "queue_id")
    private Long queueId;

    @Column(name = "ivr_flow_id")
    private Long ivrFlowId;

    @Column(name = "recording_id", columnDefinition = "UUID")
    private UUID recordingId;

    @Column(name = "sip_account_id")
    private Long sipAccountId;

    @Column(name = "trunk_id")
    private Long trunkId;

    @Column(name = "cost", precision = 10, scale = 4)
    private BigDecimal cost;

    @Column(name = "rate", precision = 10, scale = 6)
    private BigDecimal rate;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "tenant_id", columnDefinition = "UUID", nullable = false)
    private UUID tenantId;

    public enum Direction {
        INBOUND, OUTBOUND
    }

    public enum Status {
        ANSWERED, BUSY, NO_ANSWER, FAILED, CANCELED
    }
}
