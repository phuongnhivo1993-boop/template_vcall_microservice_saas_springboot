package com.vcall.survey.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.survey.dto.request.SurveyRequest;
import com.vcall.survey.dto.response.SurveyResponse;
import com.vcall.survey.entity.Survey;
import com.vcall.survey.entity.SurveyStatus;
import com.vcall.survey.mapper.SurveyMapper;
import com.vcall.survey.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final SurveyMapper mapper;

    @Transactional
    public SurveyResponse createSurvey(SurveyRequest request) {
        Survey survey = mapper.toEntity(request);
        survey = surveyRepository.save(survey);
        return mapper.toResponse(survey);
    }

    @Transactional(readOnly = true)
    public SurveyResponse getSurvey(UUID id) {
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Survey not found with id: " + id));
        return mapper.toResponse(survey);
    }

    @Transactional(readOnly = true)
    public Page<SurveyResponse> getAllSurveys(Pageable pageable) {
        return surveyRepository.findAll(pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SurveyResponse> searchSurveys(Specification<Survey> spec, Pageable pageable) {
        return surveyRepository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Transactional
    public SurveyResponse updateSurvey(UUID id, SurveyRequest request) {
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Survey not found with id: " + id));
        mapper.updateEntity(request, survey);
        survey = surveyRepository.save(survey);
        return mapper.toResponse(survey);
    }

    @Transactional
    public void deleteSurvey(UUID id) {
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Survey not found with id: " + id));
        survey.setIsDeleted(true);
        surveyRepository.save(survey);
    }

    @Transactional
    public SurveyResponse duplicateSurvey(UUID id) {
        Survey original = surveyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Survey not found with id: " + id));
        Survey copy = new Survey();
        copy.setTitle(original.getTitle() + " (Copy)");
        copy.setDescription(original.getDescription());
        copy.setType(original.getType());
        copy.setIsActive(false);
        copy.setStatus(SurveyStatus.DRAFT);
        copy = surveyRepository.save(copy);
        return mapper.toResponse(copy);
    }
}
