package com.vcall.survey.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.survey.dto.request.SurveyQuestionRequest;
import com.vcall.survey.dto.response.SurveyQuestionResponse;
import com.vcall.survey.entity.Survey;
import com.vcall.survey.entity.SurveyQuestion;
import com.vcall.survey.mapper.SurveyMapper;
import com.vcall.survey.repository.SurveyQuestionRepository;
import com.vcall.survey.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyQuestionService {

    private final SurveyQuestionRepository questionRepository;
    private final SurveyRepository surveyRepository;
    private final SurveyMapper mapper;

    @Transactional
    public SurveyQuestionResponse createQuestion(SurveyQuestionRequest request) {
        Survey survey = surveyRepository.findById(request.getSurveyId())
                .orElseThrow(() -> new ResourceNotFoundException("Survey not found with id: " + request.getSurveyId()));
        SurveyQuestion question = mapper.toEntity(request);
        question.setSurvey(survey);
        question = questionRepository.save(question);
        return mapper.toResponse(question);
    }

    @Transactional(readOnly = true)
    public SurveyQuestionResponse getQuestion(UUID id) {
        SurveyQuestion question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));
        return mapper.toResponse(question);
    }

    @Transactional(readOnly = true)
    public List<SurveyQuestionResponse> getQuestionsBySurvey(UUID surveyId) {
        return questionRepository.findBySurveyIdOrderByOrderIndex(surveyId)
                .stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public SurveyQuestionResponse updateQuestion(UUID id, SurveyQuestionRequest request) {
        SurveyQuestion question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));
        mapper.updateEntity(request, question);
        if (!question.getSurvey().getId().equals(request.getSurveyId())) {
            Survey survey = surveyRepository.findById(request.getSurveyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Survey not found with id: " + request.getSurveyId()));
            question.setSurvey(survey);
        }
        question = questionRepository.save(question);
        return mapper.toResponse(question);
    }

    @Transactional
    public void deleteQuestion(UUID id) {
        SurveyQuestion question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));
        questionRepository.delete(question);
    }

    @Transactional
    public void deleteBySurveyId(UUID surveyId) {
        questionRepository.deleteBySurveyId(surveyId);
    }
}
