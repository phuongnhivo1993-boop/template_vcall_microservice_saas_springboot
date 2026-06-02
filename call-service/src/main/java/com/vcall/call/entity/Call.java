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

import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "calls")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_deleted = false")
public class Call extends BaseEntity {

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
    private CallDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CallStatus status = CallStatus.RINGING;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "answer_time")
    private LocalDateTime answerTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration")
    private Long duration;

    @Column(name = "agent_id", columnDefinition = "UUID")
    private UUID agentId;

    @Column(name = "queue_id")
    private Long queueId;

    @Column(name = "ivr_flow_id")
    private Long ivrFlowId;

    @Column(name = "hangup_cause", length = 100)
    private String hangupCause;

    @Column(name = "recording_id", columnDefinition = "UUID")
    private UUID recordingId;

    public enum CallDirection {
        INBOUND, OUTBOUND
    }

    public enum CallStatus {
        RINGING, IN_PROGRESS, COMPLETED, FAILED, BUSY, NO_ANSWER, ON_HOLD
    }
}
