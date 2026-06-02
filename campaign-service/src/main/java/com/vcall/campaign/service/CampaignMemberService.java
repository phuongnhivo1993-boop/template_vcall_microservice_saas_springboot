package com.vcall.campaign.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcall.campaign.dto.CampaignMemberRequest;
import com.vcall.campaign.dto.CampaignMemberResponse;
import com.vcall.campaign.entity.Campaign;
import com.vcall.campaign.entity.CampaignMember;
import com.vcall.campaign.entity.CampaignMember.MemberStatus;
import com.vcall.campaign.repository.CampaignMemberRepository;
import com.vcall.campaign.repository.CampaignRepository;
import com.vcall.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CampaignMemberService {

    private final CampaignMemberRepository campaignMemberRepository;
    private final CampaignRepository campaignRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public CampaignMemberResponse addMember(Long campaignId, CampaignMemberRequest request) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campaignId));

        CampaignMember member = new CampaignMember();
        member.setCampaign(campaign);
        member.setContactName(request.getContactName());
        member.setContactPhone(request.getContactPhone());
        member.setContactEmail(request.getContactEmail());
        member.setContactData(request.getContactData());
        member.setPriority(request.getPriority() != null ? request.getPriority() : 0);
        member.setStatus(MemberStatus.PENDING);
        member.setAttempts(0);
        member = campaignMemberRepository.save(member);
        return toResponse(member);
    }

    @Transactional
    public void removeMember(Long campaignId, Long memberId) {
        CampaignMember member = campaignMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign member not found with id: " + memberId));
        if (!member.getCampaign().getId().equals(campaignId)) {
            throw new IllegalArgumentException("Member does not belong to campaign " + campaignId);
        }
        campaignMemberRepository.delete(member);
    }

    @Transactional(readOnly = true)
    public CampaignMemberResponse getMember(Long campaignId, Long memberId) {
        CampaignMember member = campaignMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign member not found with id: " + memberId));
        if (!member.getCampaign().getId().equals(campaignId)) {
            throw new IllegalArgumentException("Member does not belong to campaign " + campaignId);
        }
        return toResponse(member);
    }

    @Transactional(readOnly = true)
    public Page<CampaignMemberResponse> getMembers(Long campaignId, Pageable pageable) {
        return campaignMemberRepository.findByCampaignId(campaignId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public CampaignMemberResponse updateMemberStatus(Long memberId, MemberStatus status, String result) {
        CampaignMember member = campaignMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign member not found with id: " + memberId));
        member.setStatus(status);
        member.setResult(result);
        if (status == MemberStatus.COMPLETED || status == MemberStatus.FAILED) {
            member.setCompletedAt(java.time.LocalDateTime.now());
        }
        member = campaignMemberRepository.save(member);
        return toResponse(member);
    }

    @Transactional(readOnly = true)
    public Optional<CampaignMember> getNextMember(Long campaignId) {
        return campaignMemberRepository
                .findTopByCampaignIdAndStatusOrderByPriorityAscLastDialedAtAsc(campaignId, MemberStatus.PENDING);
    }

    @Transactional
    public List<CampaignMemberResponse> importMembers(Long campaignId, MultipartFile file) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campaignId));

        List<CampaignMember> members = new ArrayList<>();
        String filename = file.getOriginalFilename();

        try {
            if (filename != null && filename.endsWith(".json")) {
                String content = new String(file.getBytes(), StandardCharsets.UTF_8);
                List<CampaignMemberRequest> requests = objectMapper.readValue(content,
                        new TypeReference<List<CampaignMemberRequest>>() {});
                for (CampaignMemberRequest req : requests) {
                    CampaignMember member = new CampaignMember();
                    member.setCampaign(campaign);
                    member.setContactName(req.getContactName());
                    member.setContactPhone(req.getContactPhone());
                    member.setContactEmail(req.getContactEmail());
                    member.setContactData(req.getContactData());
                    member.setPriority(req.getPriority() != null ? req.getPriority() : 0);
                    member.setStatus(MemberStatus.PENDING);
                    member.setAttempts(0);
                    members.add(member);
                }
            } else {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                    String headerLine = br.readLine();
                    String[] headers = headerLine != null ? headerLine.split(",") : new String[0];
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] values = line.split(",");
                        CampaignMember member = new CampaignMember();
                        member.setCampaign(campaign);
                        member.setStatus(MemberStatus.PENDING);
                        member.setAttempts(0);
                        for (int i = 0; i < headers.length && i < values.length; i++) {
                            String header = headers[i].trim().toLowerCase();
                            String value = values[i].trim();
                            switch (header) {
                                case "name":
                                case "contact_name":
                                    member.setContactName(value);
                                    break;
                                case "phone":
                                case "contact_phone":
                                    member.setContactPhone(value);
                                    break;
                                case "email":
                                case "contact_email":
                                    member.setContactEmail(value);
                                    break;
                                case "priority":
                                    member.setPriority(Integer.parseInt(value));
                                    break;
                                default:
                                    break;
                            }
                        }
                        members.add(member);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to import members from file: " + e.getMessage(), e);
        }

        members = campaignMemberRepository.saveAll(members);
        return members.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private CampaignMemberResponse toResponse(CampaignMember member) {
        return CampaignMemberResponse.builder()
                .id(member.getId())
                .campaignId(member.getCampaign().getId())
                .contactName(member.getContactName())
                .contactPhone(member.getContactPhone())
                .contactEmail(member.getContactEmail())
                .priority(member.getPriority())
                .status(member.getStatus().name())
                .attempts(member.getAttempts())
                .lastDialedAt(member.getLastDialedAt())
                .result(member.getResult())
                .build();
    }
}
