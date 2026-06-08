package com.vcall.campaign.controller;

import com.vcall.campaign.dto.CampaignResultResponse;
import com.vcall.campaign.service.CampaignResultService;
import com.vcall.common.dto.ApiResponse;
import com.vcall.common.util.BulkOperationUtil;
import com.vcall.common.util.CsvExportUtil;
import com.vcall.common.util.ExcelExportUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/campaigns/{campaignId}/results")
@RequiredArgsConstructor
public class CampaignResultController {

    private final CampaignResultService campaignResultService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<CampaignResultResponse>>> getResults(
            @PathVariable Long campaignId, Pageable pageable) {
        Page<CampaignResultResponse> results = campaignResultService.getResults(campaignId, pageable);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/agent/{agentId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<CampaignResultResponse>>> getAgentResults(
            @PathVariable Long campaignId,
            @PathVariable UUID agentId, Pageable pageable) {
        Page<CampaignResultResponse> results = campaignResultService.getAgentResults(agentId, pageable);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<CampaignResultResponse>>> search(
            @PathVariable Long campaignId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String resultType,
            Pageable pageable) {
        Page<CampaignResultResponse> result = campaignResultService.search(campaignId, keyword, resultType, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/export/csv")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public void exportCsv(@PathVariable Long campaignId,
                          @RequestParam(required = false) String keyword,
                          HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<CampaignResultResponse> items = campaignResultService.search(campaignId, keyword, null, pageable);
        List<String> headers = Arrays.asList("ID", "Campaign ID", "Agent ID", "Call ID", "Result Type", "Duration", "Notes", "Disposition", "Created At");
        List<List<String>> rows = CsvExportUtil.toRows(items.getContent(),
                Arrays.asList("id", "campaignId", "agentId", "callId", "resultType", "duration", "notes", "disposition", "createdAt"));
        CsvExportUtil.writeCsv(response, "campaign-results.csv", headers, rows);
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public void exportExcel(@PathVariable Long campaignId,
                            @RequestParam(required = false) String keyword,
                            HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<CampaignResultResponse> items = campaignResultService.search(campaignId, keyword, null, pageable);
        List<String> headers = Arrays.asList("ID", "Campaign ID", "Agent ID", "Call ID", "Result Type", "Duration", "Notes", "Disposition", "Created At");
        ExcelExportUtil.writeExcel(response, "campaign-results.xlsx", headers, items.getContent(),
                Arrays.asList("id", "campaignId", "agentId", "callId", "resultType", "duration", "notes", "disposition", "createdAt"));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats(@PathVariable Long campaignId) {
        return ResponseEntity.ok(ApiResponse.success(campaignResultService.getStats(campaignId)));
    }

    @DeleteMapping("/{resultId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Void>> deleteResult(@PathVariable Long resultId) {
        campaignResultService.deleteResult(resultId);
        return ResponseEntity.ok(ApiResponse.success("Result deleted", null));
    }

    @PostMapping("/bulk-delete")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<BulkOperationUtil.BulkResult<Long>>> bulkDelete(
            @PathVariable Long campaignId, @RequestBody List<Long> ids) {
        BulkOperationUtil.BulkResult<Long> result = new BulkOperationUtil.BulkResult<>();
        for (Long id : ids) {
            try {
                campaignResultService.deleteResult(id);
                result.addSuccess(id);
            } catch (Exception e) {
                result.addFailure(id, e.getMessage());
            }
        }
        return ResponseEntity.ok(ApiResponse.success("Bulk delete completed", result));
    }
}
