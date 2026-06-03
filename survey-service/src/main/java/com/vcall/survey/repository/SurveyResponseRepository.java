package com.vcall.survey.repository;

import com.vcall.survey.entity.SurveyResponseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SurveyResponseRepository extends JpaRepository<SurveyResponseEntity, UUID>, JpaSpecificationExecutor<SurveyResponseEntity> {

    List<SurveyResponseEntity> findBySurveyId(UUID surveyId);

    List<SurveyResponseEntity> findByCustomerId(UUID customerId);

    List<SurveyResponseEntity> findByQuestionId(UUID questionId);

    List<SurveyResponseEntity> findBySurveyIdAndSubmittedAtBetween(UUID surveyId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT AVG(sr.rating) FROM SurveyResponseEntity sr WHERE sr.surveyId = :surveyId AND sr.rating IS NOT NULL")
    Double averageRatingBySurveyId(UUID surveyId);

    @Query("SELECT COUNT(sr) FROM SurveyResponseEntity sr WHERE sr.surveyId = :surveyId AND sr.rating >= 9")
    long countPromoters(UUID surveyId);

    @Query("SELECT COUNT(sr) FROM SurveyResponseEntity sr WHERE sr.surveyId = :surveyId AND sr.rating >= 7 AND sr.rating <= 8")
    long countPassives(UUID surveyId);

    @Query("SELECT COUNT(sr) FROM SurveyResponseEntity sr WHERE sr.surveyId = :surveyId AND sr.rating <= 6")
    long countDetractors(UUID surveyId);

    long countBySurveyId(UUID surveyId);
}
