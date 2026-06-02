package com.vcall.sms.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.common.exception.DuplicateResourceException;
import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.sms.dto.SmsTemplateRequest;
import com.vcall.sms.dto.SmsTemplateResponse;
import com.vcall.sms.entity.SmsTemplate;
import com.vcall.sms.repository.SmsTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SmsTemplateService {

    private final SmsTemplateRepository smsTemplateRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public SmsTemplateResponse createTemplate(SmsTemplateRequest request) {
        if (smsTemplateRepository.findByName(request.getName()).isPresent()) {
            throw new DuplicateResourceException("Template already exists with name: " + request.getName());
        }

        SmsTemplate template = new SmsTemplate();
        template.setName(request.getName());
        template.setContent(request.getContent());
        template.setVariables(request.getVariables());
        template.setActive(request.isActive());

        template = smsTemplateRepository.save(template);
        return toResponse(template);
    }

    public SmsTemplateResponse getTemplate(Long id) {
        SmsTemplate template = smsTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));
        return toResponse(template);
    }

    public List<SmsTemplateResponse> getAllTemplates() {
        return smsTemplateRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<SmsTemplateResponse> getActiveTemplates() {
        return smsTemplateRepository.findByIsActiveTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SmsTemplateResponse updateTemplate(Long id, SmsTemplateRequest request) {
        SmsTemplate template = smsTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));

        if (!template.getName().equals(request.getName()) &&
                smsTemplateRepository.findByName(request.getName()).isPresent()) {
            throw new DuplicateResourceException("Template already exists with name: " + request.getName());
        }

        template.setName(request.getName());
        template.setContent(request.getContent());
        template.setVariables(request.getVariables());
        template.setActive(request.isActive());

        template = smsTemplateRepository.save(template);
        return toResponse(template);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        SmsTemplate template = smsTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));
        template.setIsDeleted(true);
        smsTemplateRepository.save(template);
    }

    public String renderTemplate(Long templateId, Map<String, String> variables) {
        SmsTemplate template = smsTemplateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + templateId));

        String content = template.getContent();
        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                content = content.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }
        }
        return content;
    }

    private SmsTemplateResponse toResponse(SmsTemplate template) {
        return SmsTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .content(template.getContent())
                .variables(template.getVariables())
                .isActive(template.isActive())
                .createdAt(template.getCreatedAt())
                .build();
    }
}
