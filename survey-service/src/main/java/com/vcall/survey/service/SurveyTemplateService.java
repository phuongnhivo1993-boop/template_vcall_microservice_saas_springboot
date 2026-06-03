package com.vcall.survey.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.survey.dto.request.SurveyTemplateRequest;
import com.vcall.survey.dto.response.SurveyTemplateResponse;
import com.vcall.survey.entity.SurveyTemplate;
import com.vcall.survey.mapper.SurveyMapper;
import com.vcall.survey.repository.SurveyTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SurveyTemplateService {

    private final SurveyTemplateRepository templateRepository;
    private final SurveyMapper mapper;

    @Transactional
    public SurveyTemplateResponse createTemplate(SurveyTemplateRequest request) {
        SurveyTemplate template = mapper.toEntity(request);
        template = templateRepository.save(template);
        return mapper.toResponse(template);
    }

    @Transactional(readOnly = true)
    public SurveyTemplateResponse getTemplate(UUID id) {
        SurveyTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Survey template not found with id: " + id));
        return mapper.toResponse(template);
    }

    @Transactional(readOnly = true)
    public Page<SurveyTemplateResponse> getAllTemplates(Pageable pageable) {
        return templateRepository.findAll(pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SurveyTemplateResponse> searchTemplates(Specification<SurveyTemplate> spec, Pageable pageable) {
        return templateRepository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Transactional
    public SurveyTemplateResponse updateTemplate(UUID id, SurveyTemplateRequest request) {
        SurveyTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Survey template not found with id: " + id));
        mapper.updateEntity(request, template);
        template = templateRepository.save(template);
        return mapper.toResponse(template);
    }

    @Transactional
    public void deleteTemplate(UUID id) {
        SurveyTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Survey template not found with id: " + id));
        template.setIsDeleted(true);
        templateRepository.save(template);
    }
}
