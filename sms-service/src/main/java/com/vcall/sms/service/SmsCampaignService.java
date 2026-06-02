package com.vcall.sms.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.sms.dto.SmsCampaignRequest;
import com.vcall.sms.dto.SmsCampaignResponse;
import com.vcall.sms.dto.SmsRequest;
import com.vcall.sms.entity.SmsCampaign;
import com.vcall.sms.entity.SmsTemplate;
import com.vcall.sms.kafka.SmsEventPublisher;
import com.vcall.sms.repository.SmsCampaignRepository;
import com.vcall.sms.repository.SmsTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsCampaignService {

    private final SmsCampaignRepository smsCampaignRepository;
    private final SmsTemplateRepository smsTemplateRepository;
    private final SmsService smsService;
    private final SmsEventPublisher smsEventPublisher;
    private final ObjectMapper objectMapper;

    @Transactional
    public SmsCampaignResponse createCampaign(SmsCampaignRequest request) {
        SmsTemplate template = smsTemplateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + request.getTemplateId()));

        SmsCampaign campaign = new SmsCampaign();
        campaign.setName(request.getName());
        campaign.setDescription(request.getDescription());
        campaign.setTemplate(template);
        campaign.setRecipientList(request.getRecipientList());
        campaign.setScheduledAt(request.getScheduledAt());

        List<String> recipients = parseRecipients(request.getRecipientList());
        campaign.setTotalRecipients(recipients.size());

        if (request.getScheduledAt() != null && request.getScheduledAt().isAfter(LocalDateTime.now())) {
            campaign.setStatus(SmsCampaign.CampaignStatus.SCHEDULED);
        }

        campaign = smsCampaignRepository.save(campaign);
        return toResponse(campaign);
    }

    public SmsCampaignResponse getCampaign(Long id) {
        SmsCampaign campaign = smsCampaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));
        return toResponse(campaign);
    }

    public Page<SmsCampaignResponse> getAllCampaigns(Pageable pageable) {
        return smsCampaignRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional
    public SmsCampaignResponse updateCampaign(Long id, SmsCampaignRequest request) {
        SmsCampaign campaign = smsCampaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));

        SmsTemplate template = smsTemplateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + request.getTemplateId()));

        campaign.setName(request.getName());
        campaign.setDescription(request.getDescription());
        campaign.setTemplate(template);
        campaign.setRecipientList(request.getRecipientList());
        campaign.setScheduledAt(request.getScheduledAt());

        List<String> recipients = parseRecipients(request.getRecipientList());
        campaign.setTotalRecipients(recipients.size());

        campaign = smsCampaignRepository.save(campaign);
        return toResponse(campaign);
    }

    @Transactional
    public void deleteCampaign(Long id) {
        SmsCampaign campaign = smsCampaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));
        campaign.setIsDeleted(true);
        smsCampaignRepository.save(campaign);
    }

    @Transactional
    public SmsCampaignResponse startCampaign(Long id) {
        SmsCampaign campaign = smsCampaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));

        if (campaign.getStatus() != SmsCampaign.CampaignStatus.DRAFT &&
                campaign.getStatus() != SmsCampaign.CampaignStatus.SCHEDULED &&
                campaign.getStatus() != SmsCampaign.CampaignStatus.PAUSED) {
            throw new IllegalStateException("Campaign cannot be started in status: " + campaign.getStatus());
        }

        campaign.setStatus(SmsCampaign.CampaignStatus.RUNNING);
        campaign.setStartedAt(LocalDateTime.now());
        campaign = smsCampaignRepository.save(campaign);

        smsEventPublisher.publishCampaignStarted(campaign);

        processCampaign(campaign);

        return toResponse(campaign);
    }

    @Transactional
    public SmsCampaignResponse pauseCampaign(Long id) {
        SmsCampaign campaign = smsCampaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));

        if (campaign.getStatus() != SmsCampaign.CampaignStatus.RUNNING) {
            throw new IllegalStateException("Only running campaigns can be paused");
        }

        campaign.setStatus(SmsCampaign.CampaignStatus.PAUSED);
        campaign = smsCampaignRepository.save(campaign);

        return toResponse(campaign);
    }

    @Transactional
    public void processCampaign(SmsCampaign campaign) {
        try {
            List<String> recipients = parseRecipients(campaign.getRecipientList());
            int sentCount = 0;
            int failedCount = 0;

            for (String recipient : recipients) {
                try {
                    SmsRequest smsRequest = SmsRequest.builder()
                            .fromNumber(campaign.getTemplate().getName())
                            .toNumber(recipient.trim())
                            .content(campaign.getTemplate().getContent())
                            .templateId(campaign.getTemplate().getId())
                            .build();
                    smsService.sendSms(smsRequest);
                    sentCount++;
                } catch (Exception e) {
                    log.error("Failed to send SMS to {}: {}", recipient, e.getMessage());
                    failedCount++;
                }
            }

            campaign.setSentCount(sentCount);
            campaign.setFailedCount(failedCount);
            campaign.setStatus(SmsCampaign.CampaignStatus.COMPLETED);
            campaign.setCompletedAt(LocalDateTime.now());
            smsCampaignRepository.save(campaign);

            smsEventPublisher.publishCampaignFinished(campaign);
        } catch (Exception e) {
            log.error("Campaign processing failed: {}", e.getMessage());
            campaign.setStatus(SmsCampaign.CampaignStatus.FAILED);
            smsCampaignRepository.save(campaign);
        }
    }

    public Page<SmsCampaignResponse> getCampaignsByStatus(SmsCampaign.CampaignStatus status, Pageable pageable) {
        return smsCampaignRepository.findByStatus(status, pageable)
                .map(this::toResponse);
    }

    private List<String> parseRecipients(String recipientList) {
        try {
            return objectMapper.readValue(recipientList, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of(recipientList.split(","));
        }
    }

    private SmsCampaignResponse toResponse(SmsCampaign campaign) {
        return SmsCampaignResponse.builder()
                .id(campaign.getId())
                .name(campaign.getName())
                .description(campaign.getDescription())
                .status(campaign.getStatus().name())
                .scheduledAt(campaign.getScheduledAt())
                .startedAt(campaign.getStartedAt())
                .completedAt(campaign.getCompletedAt())
                .totalRecipients(campaign.getTotalRecipients())
                .sentCount(campaign.getSentCount())
                .failedCount(campaign.getFailedCount())
                .createdAt(campaign.getCreatedAt())
                .build();
    }
}
