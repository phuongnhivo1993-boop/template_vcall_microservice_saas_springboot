package com.vcall.survey.repository;

import com.vcall.survey.entity.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, UUID>, JpaSpecificationExecutor<Survey> {

    List<Survey> findByIsActiveTrue();

    List<Survey> findByType(Survey.SurveyType type);
}
