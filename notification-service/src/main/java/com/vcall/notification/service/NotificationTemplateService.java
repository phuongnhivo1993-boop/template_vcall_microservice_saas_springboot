package com.vcall.notification.service;

import com.vcall.common.exception.DuplicateResourceException;
import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.notification.dto.NotificationTemplateRequest;
import com.vcall.notification.dto.NotificationTemplateResponse;
import com.vcall.notification.entity.NotificationChannel;
import com.vcall.notification.entity.NotificationTemplate;
import com.vcall.notification.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationTemplateService {

    private final NotificationTemplateRepository templateRepository;

    @Transactional
    public NotificationTemplateResponse create(NotificationTemplateRequest request) {
        if (templateRepository.findByName(request.getName()).isPresent()) {
            throw new DuplicateResourceException("Template already exists with name: " + request.getName());
        }
        NotificationTemplate template = new NotificationTemplate();
        template.setName(request.getName());
        template.setChannel(request.getChannel());
        template.setTitle(request.getTitle());
        template.setBody(request.getBody());
        template.setVariables(request.getVariables());
        template.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        template = templateRepository.save(template);
        return toResponse(template);
    }

    @Transactional(readOnly = true)
    public NotificationTemplateResponse getById(Long id) {
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));
        return toResponse(template);
    }

    @Transactional(readOnly = true)
    public List<NotificationTemplateResponse> getAll() {
        return templateRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public NotificationTemplateResponse update(Long id, NotificationTemplateRequest request) {
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));
        template.setName(request.getName());
        template.setChannel(request.getChannel());
        template.setTitle(request.getTitle());
        template.setBody(request.getBody());
        template.setVariables(request.getVariables());
        template.setIsActive(request.getIsActive());
        template = templateRepository.save(template);
        return toResponse(template);
    }

    @Transactional
    public void delete(Long id) {
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));
        templateRepository.delete(template);
    }

    public String renderTemplate(String templateContent, Map<String, String> variables) {
        if (templateContent == null) return null;
        String result = templateContent;
        Pattern pattern = Pattern.compile("\\{\\{\\s*(\\w+)\\s*}}");
        Matcher matcher = pattern.matcher(result);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = variables.getOrDefault(key, "");
            result = result.replace("{{" + key + "}}", value);
        }
        return result;
    }

    private NotificationTemplateResponse toResponse(NotificationTemplate template) {
        return NotificationTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .channel(template.getChannel())
                .title(template.getTitle())
                .body(template.getBody())
                .variables(template.getVariables())
                .isActive(template.getIsActive())
                .build();
    }
}
