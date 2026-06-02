package com.vcall.call.entity;

import com.vcall.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "call_evaluations")
public class CallEvaluation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "call_id", nullable = false)
    private UUID callId;

    @Column(name = "evaluator_id")
    private UUID evaluatorId;

    @Column(name = "evaluator_name", length = 255)
    private String evaluatorName;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "max_score", nullable = false)
    @Builder.Default
    private Integer maxScore = 100;

    @Column(name = "greeting_score", columnDefinition = "integer default 0")
    private Integer greetingScore;

    @Column(name = "knowledge_score", columnDefinition = "integer default 0")
    private Integer knowledgeScore;

    @Column(name = "resolution_score", columnDefinition = "integer default 0")
    private Integer resolutionScore;

    @Column(name = "communication_score", columnDefinition = "integer default 0")
    private Integer communicationScore;

    @Column(name = "empathy_score", columnDefinition = "integer default 0")
    private Integer empathyScore;

    @Column(name = "compliance_score", columnDefinition = "integer default 0")
    private Integer complianceScore;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths;

    @Column(name = "improvements", columnDefinition = "TEXT")
    private String improvements;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "DRAFT";

    @Column(name = "evaluation_date")
    private java.time.LocalDateTime evaluationDate;
}
