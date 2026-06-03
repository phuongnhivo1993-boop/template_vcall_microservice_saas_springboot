package com.vcall.survey.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.survey.dto.request.SurveyQuestionRequest;
import com.vcall.survey.dto.response.SurveyQuestionResponse;
import com.vcall.survey.service.SurveyQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/survey-questions")
@RequiredArgsConstructor
@Tag(name = "Survey Question Management", description = "CRUD for survey questions")
public class SurveyQuestionController {

    private final SurveyQuestionService questionService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Create a survey question")
    public ResponseEntity<ApiResponse<SurveyQuestionResponse>> createQuestion(@Valid @RequestBody SurveyQuestionRequest request) {
        SurveyQuestionResponse response = questionService.createQuestion(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Question created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get question by ID")
    public ResponseEntity<ApiResponse<SurveyQuestionResponse>> getQuestion(@PathVariable UUID id) {
        SurveyQuestionResponse response = questionService.getQuestion(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/by-survey/{surveyId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get questions by survey ID")
    public ResponseEntity<ApiResponse<List<SurveyQuestionResponse>>> getBySurvey(@PathVariable UUID surveyId) {
        List<SurveyQuestionResponse> questions = questionService.getQuestionsBySurvey(surveyId);
        return ResponseEntity.ok(ApiResponse.success(questions));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Update a survey question")
    public ResponseEntity<ApiResponse<SurveyQuestionResponse>> updateQuestion(@PathVariable UUID id,
                                                                               @Valid @RequestBody SurveyQuestionRequest request) {
        SurveyQuestionResponse response = questionService.updateQuestion(id, request);
        return ResponseEntity.ok(ApiResponse.success("Question updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Delete a survey question")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(@PathVariable UUID id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.ok(ApiResponse.success("Question deleted successfully", null));
    }

    @DeleteMapping("/by-survey/{surveyId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Delete all questions for a survey")
    public ResponseEntity<ApiResponse<Void>> deleteBySurvey(@PathVariable UUID surveyId) {
        questionService.deleteBySurveyId(surveyId);
        return ResponseEntity.ok(ApiResponse.success("Questions deleted successfully", null));
    }
}
