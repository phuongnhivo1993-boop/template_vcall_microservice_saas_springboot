package com.vcall.scheduling.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.scheduling.dto.request.ScheduleTemplateRequest;
import com.vcall.scheduling.dto.response.ScheduleTemplateResponse;
import com.vcall.scheduling.entity.ScheduleTemplate;
import com.vcall.scheduling.mapper.SchedulingMapper;
import com.vcall.scheduling.repository.ScheduleTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleTemplateService {

    private final ScheduleTemplateRepository scheduleTemplateRepository;
    private final SchedulingMapper mapper;

    @Transactional
    public ScheduleTemplateResponse createTemplate(ScheduleTemplateRequest request) {
        ScheduleTemplate template = mapper.toEntity(request);
        template = scheduleTemplateRepository.save(template);
        return mapper.toResponse(template);
    }

    @Transactional(readOnly = true)
    public ScheduleTemplateResponse getTemplate(UUID id) {
        ScheduleTemplate template = scheduleTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule template not found with id: " + id));
        return mapper.toResponse(template);
    }

    @Transactional(readOnly = true)
    public Page<ScheduleTemplateResponse> getAllTemplates(Pageable pageable) {
        return scheduleTemplateRepository.findAll(pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ScheduleTemplateResponse> searchTemplates(Specification<ScheduleTemplate> spec, Pageable pageable) {
        return scheduleTemplateRepository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Transactional
    public ScheduleTemplateResponse updateTemplate(UUID id, ScheduleTemplateRequest request) {
        ScheduleTemplate template = scheduleTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule template not found with id: " + id));
        mapper.updateEntity(request, template);
        template = scheduleTemplateRepository.save(template);
        return mapper.toResponse(template);
    }

    @Transactional
    public void deleteTemplate(UUID id) {
        ScheduleTemplate template = scheduleTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule template not found with id: " + id));
        template.setIsDeleted(true);
        scheduleTemplateRepository.save(template);
    }

    @Transactional(readOnly = true)
    public List<ScheduleTemplateResponse> getByAgent(UUID agentId) {
        return scheduleTemplateRepository.findByAgentId(agentId)
                .stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ScheduleTemplateResponse> getByAgentAndDay(UUID agentId, DayOfWeek dayOfWeek) {
        return scheduleTemplateRepository.findByAgentIdAndDayOfWeek(agentId, dayOfWeek)
                .stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ScheduleTemplateResponse> getActiveByAgent(UUID agentId) {
        return scheduleTemplateRepository.findByAgentIdAndIsActiveTrue(agentId)
                .stream().map(mapper::toResponse).collect(Collectors.toList());
    }
}
