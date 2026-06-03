package com.vcall.survey.repository;

import com.vcall.survey.entity.SurveyTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SurveyTemplateRepository extends JpaRepository<SurveyTemplate, UUID>, JpaSpecificationExecutor<SurveyTemplate> {

    List<SurveyTemplate> findByIsActiveTrue();

    List<SurveyTemplate> findByTrigger(SurveyTemplate.TriggerType trigger);
}
