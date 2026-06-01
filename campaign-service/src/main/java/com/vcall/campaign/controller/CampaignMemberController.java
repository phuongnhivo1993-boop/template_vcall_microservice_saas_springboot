package com.vcall.campaign.controller;

import com.vcall.campaign.dto.CampaignMemberRequest;
import com.vcall.campaign.dto.CampaignMemberResponse;
import com.vcall.campaign.service.CampaignMemberService;
import com.vcall.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/campaigns/{campaignId}/members")
@RequiredArgsConstructor
public class CampaignMemberController {

    private final CampaignMemberService campaignMemberService;

    @PostMapping
    public ResponseEntity<ApiResponse<CampaignMemberResponse>> addMember(
            @PathVariable Long campaignId,
            @Valid @RequestBody CampaignMemberRequest request) {
        CampaignMemberResponse response = campaignMemberService.addMember(campaignId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Member added successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CampaignMemberResponse>>> getMembers(
            @PathVariable Long campaignId) {
        List<CampaignMemberResponse> members = campaignMemberService.getMembers(campaignId);
        return ResponseEntity.ok(ApiResponse.success(members));
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<ApiResponse<CampaignMemberResponse>> getMember(
            @PathVariable Long campaignId,
            @PathVariable Long memberId) {
        CampaignMemberResponse response = campaignMemberService.getMember(campaignId, memberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long campaignId,
            @PathVariable Long memberId) {
        campaignMemberService.removeMember(campaignId, memberId);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully", null));
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<CampaignMemberResponse>>> importMembers(
            @PathVariable Long campaignId,
            @RequestParam("file") MultipartFile file) {
        List<CampaignMemberResponse> members = campaignMemberService.importMembers(campaignId, file);
        return ResponseEntity.ok(ApiResponse.success("Members imported successfully", members));
    }
}
