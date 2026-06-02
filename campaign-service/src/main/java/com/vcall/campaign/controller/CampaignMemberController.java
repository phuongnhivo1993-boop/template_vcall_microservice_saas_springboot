package com.vcall.campaign.controller;

import com.vcall.campaign.dto.CampaignMemberRequest;
import com.vcall.campaign.dto.CampaignMemberResponse;
import com.vcall.campaign.service.CampaignMemberService;
import com.vcall.common.dto.ApiResponse;
import com.vcall.common.util.CsvExportUtil;
import com.vcall.common.util.ExcelExportUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<ApiResponse<Page<CampaignMemberResponse>>> getMembers(
            @PathVariable Long campaignId, Pageable pageable) {
        Page<CampaignMemberResponse> members = campaignMemberService.getMembers(campaignId, pageable);
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

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<CampaignMemberResponse>>> search(
            @PathVariable Long campaignId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        Page<CampaignMemberResponse> result = campaignMemberService.search(campaignId, keyword, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/export/csv")
    public void exportCsv(@PathVariable Long campaignId,
                          @RequestParam(required = false) String keyword,
                          HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<CampaignMemberResponse> items = campaignMemberService.search(campaignId, keyword, null, pageable);
        List<String> headers = Arrays.asList("ID", "Campaign ID", "Contact Name", "Contact Phone", "Contact Email", "Priority", "Status", "Attempts", "Last Dialed", "Result");
        List<List<String>> rows = CsvExportUtil.toRows(items.getContent(),
                Arrays.asList("id", "campaignId", "contactName", "contactPhone", "contactEmail", "priority", "status", "attempts", "lastDialedAt", "result"));
        CsvExportUtil.writeCsv(response, "campaign-members.csv", headers, rows);
    }

    @GetMapping("/export/excel")
    public void exportExcel(@PathVariable Long campaignId,
                            @RequestParam(required = false) String keyword,
                            HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<CampaignMemberResponse> items = campaignMemberService.search(campaignId, keyword, null, pageable);
        List<String> headers = Arrays.asList("ID", "Campaign ID", "Contact Name", "Contact Phone", "Contact Email", "Priority", "Status", "Attempts", "Last Dialed", "Result");
        ExcelExportUtil.writeExcel(response, "campaign-members.xlsx", headers, items.getContent(),
                Arrays.asList("id", "campaignId", "contactName", "contactPhone", "contactEmail", "priority", "status", "attempts", "lastDialedAt", "result"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats(@PathVariable Long campaignId) {
        return ResponseEntity.ok(ApiResponse.success(campaignMemberService.getStats(campaignId)));
    }
}
