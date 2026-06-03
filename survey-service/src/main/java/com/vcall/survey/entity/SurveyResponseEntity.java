package com.vcall.survey.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "survey_responses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyResponseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "survey_id", columnDefinition = "UUID", nullable = false)
    private UUID surveyId;

    @Column(name = "question_id", columnDefinition = "UUID", nullable = false)
    private UUID questionId;

    @Column(name = "customer_id", columnDefinition = "UUID")
    private UUID customerId;

    @Column(name = "call_id", columnDefinition = "UUID")
    private UUID callId;

    @Column(name = "ticket_id", columnDefinition = "UUID")
    private UUID ticketId;

    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;
}
