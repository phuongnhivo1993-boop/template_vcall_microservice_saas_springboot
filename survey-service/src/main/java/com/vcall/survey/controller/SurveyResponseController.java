package com.vcall.survey.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.common.util.CsvExportUtil;
import com.vcall.common.util.ExcelExportUtil;
import com.vcall.survey.dto.request.SurveyAnswerRequest;
import com.vcall.survey.dto.response.SurveyAnswerResponse;
import com.vcall.survey.entity.SurveyResponseEntity;
import com.vcall.survey.service.SurveyResponseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/survey-responses")
@RequiredArgsConstructor
@Tag(name = "Survey Response Management", description = "Submit and query survey responses")
public class SurveyResponseController {

    private final SurveyResponseService responseService;

    @PostMapping
    @Operation(summary = "Submit a survey answer")
    public ResponseEntity<ApiResponse<SurveyAnswerResponse>> submitAnswer(@Valid @RequestBody SurveyAnswerRequest request) {
        SurveyAnswerResponse response = responseService.submitAnswer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Answer submitted successfully", response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all survey responses")
    public ResponseEntity<ApiResponse<Page<SurveyAnswerResponse>>> getAllAnswers(Pageable pageable) {
        Page<SurveyAnswerResponse> responses = responseService.getAllAnswers(pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get survey response by ID")
    public ResponseEntity<ApiResponse<SurveyAnswerResponse>> getAnswer(@PathVariable UUID id) {
        SurveyAnswerResponse response = responseService.getAnswer(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search survey responses")
    public ResponseEntity<ApiResponse<Page<SurveyAnswerResponse>>> search(
            @RequestParam(required = false) UUID surveyId,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID callId,
            @RequestParam(required = false) UUID ticketId,
            Pageable pageable) {
        Specification<SurveyResponseEntity> spec = Specification.where(null);
        if (surveyId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("surveyId"), surveyId));
        }
        if (customerId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("customerId"), customerId));
        }
        if (callId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("callId"), callId));
        }
        if (ticketId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("ticketId"), ticketId));
        }
        Page<SurveyAnswerResponse> response = responseService.searchAnswers(spec, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/export/csv")
    @Operation(summary = "Export survey responses to CSV")
    public void exportCsv(@RequestParam(required = false) UUID surveyId,
                          HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("submittedAt").descending());
        Specification<SurveyResponseEntity> spec = Specification.where(null);
        if (surveyId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("surveyId"), surveyId));
        }
        Page<SurveyAnswerResponse> items = responseService.searchAnswers(spec, pageable);
        List<String> headers = Arrays.asList("ID", "Survey ID", "Question ID", "Customer ID", "Call ID", "Answer", "Rating", "Submitted At");
        List<List<String>> rows = CsvExportUtil.toRows(items.getContent(),
                Arrays.asList("id", "surveyId", "questionId", "customerId", "callId", "answer", "rating", "submittedAt"));
        CsvExportUtil.writeCsv(response, "survey-responses.csv", headers, rows);
    }

    @GetMapping("/export/excel")
    @Operation(summary = "Export survey responses to Excel")
    public void exportExcel(@RequestParam(required = false) UUID surveyId,
                            HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("submittedAt").descending());
        Specification<SurveyResponseEntity> spec = Specification.where(null);
        if (surveyId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("surveyId"), surveyId));
        }
        Page<SurveyAnswerResponse> items = responseService.searchAnswers(spec, pageable);
        List<String> headers = Arrays.asList("ID", "Survey ID", "Question ID", "Customer ID", "Call ID", "Answer", "Rating", "Submitted At");
        ExcelExportUtil.writeExcel(response, "survey-responses.xlsx", headers, items.getContent(),
                Arrays.asList("id", "surveyId", "questionId", "customerId", "callId", "answer", "rating", "submittedAt"));
    }
}
