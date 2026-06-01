package com.vcall.email.service;

import com.vcall.common.exception.DuplicateResourceException;
import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.email.dto.EmailTemplateRequest;
import com.vcall.email.dto.EmailTemplateResponse;
import com.vcall.email.entity.EmailTemplate;
import com.vcall.email.repository.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmailTemplateService {

    private final EmailTemplateRepository emailTemplateRepository;

    @Transactional
    public EmailTemplateResponse createTemplate(EmailTemplateRequest request) {
        if (emailTemplateRepository.findByName(request.getName()).isPresent()) {
            throw new DuplicateResourceException("Template already exists with name: " + request.getName());
        }

        EmailTemplate template = new EmailTemplate();
        template.setName(request.getName());
        template.setSubject(request.getSubject());
        template.setBodyHtml(request.getBodyHtml());
        template.setBodyText(request.getBodyText());
        template.setVariables(request.getVariables());
        template.setCategory(request.getCategory());
        template.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        template = emailTemplateRepository.save(template);
        return toResponse(template);
    }

    @Transactional
    public EmailTemplateResponse updateTemplate(Long id, EmailTemplateRequest request) {
        EmailTemplate template = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));

        template.setName(request.getName());
        template.setSubject(request.getSubject());
        template.setBodyHtml(request.getBodyHtml());
        template.setBodyText(request.getBodyText());
        template.setVariables(request.getVariables());
        template.setCategory(request.getCategory());
        template.setIsActive(request.getIsActive() != null ? request.getIsActive() : template.getIsActive());

        template = emailTemplateRepository.save(template);
        return toResponse(template);
    }

    @Transactional(readOnly = true)
    public EmailTemplateResponse getTemplate(Long id) {
        EmailTemplate template = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));
        return toResponse(template);
    }

    @Transactional(readOnly = true)
    public List<EmailTemplateResponse> getAllTemplates() {
        return emailTemplateRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmailTemplateResponse> getByCategory(String category) {
        return emailTemplateRepository.findByCategory(category).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteTemplate(Long id) {
        EmailTemplate template = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));
        emailTemplateRepository.delete(template);
    }

    @Transactional(readOnly = true)
    public String renderTemplate(Long templateId, Map<String, String> variables) {
        EmailTemplate template = emailTemplateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + templateId));

        String renderedHtml = template.getBodyHtml();
        String renderedText = template.getBodyText();

        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                String placeholder = "\\{\\{" + entry.getKey() + "\\}\\}";
                renderedHtml = renderedHtml != null ? renderedHtml.replaceAll(placeholder, entry.getValue()) : null;
                renderedText = renderedText != null ? renderedText.replaceAll(placeholder, entry.getValue()) : null;
            }
        }

        return renderedHtml != null ? renderedHtml : renderedText;
    }

    private EmailTemplateResponse toResponse(EmailTemplate template) {
        return EmailTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .subject(template.getSubject())
                .category(template.getCategory())
                .isActive(template.getIsActive())
                .createdAt(template.getCreatedAt())
                .build();
    }
}
