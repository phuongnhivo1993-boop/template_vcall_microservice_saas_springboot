package com.vcall.call.entity;

import com.vcall.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "call_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CallStatusHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "call_id", nullable = false)
    private Call call;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status")
    private Call.CallStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status")
    private Call.CallStatus toStatus;

    @Column(name = "changed_by")
    private UUID changedBy;

    @Column(name = "reason")
    private String reason;

    @Column(name = "changed_at")
    private LocalDateTime changedAt = LocalDateTime.now();
}
