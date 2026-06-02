package com.vcall.call.controller;

import com.vcall.call.dto.CallEvaluationRequest;
import com.vcall.call.dto.CallEvaluationResponse;
import com.vcall.call.dto.CallRequest;
import com.vcall.call.dto.CallResponse;
import com.vcall.call.dto.CallStatusRequest;
import com.vcall.call.dto.SatisfactionRequest;
import com.vcall.call.dto.TransferRequest;
import com.vcall.call.entity.Call;
import com.vcall.call.service.CallService;
import com.vcall.call.service.EvaluationService;
import com.vcall.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/calls")
@RequiredArgsConstructor
public class CallController {

    private final CallService callService;
    private final EvaluationService evaluationService;

    @PostMapping
    public ResponseEntity<CallResponse> initiateCall(@Valid @RequestBody CallRequest request) {
        CallResponse response = callService.createCall(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CallResponse> getCall(@PathVariable UUID id) {
        return ResponseEntity.ok(callService.getCall(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<CallResponse> updateCallStatus(@PathVariable UUID id,
                                                          @Valid @RequestBody CallStatusRequest request) {
        return ResponseEntity.ok(callService.updateCallStatus(id, request));
    }

    @PostMapping("/{id}/hangup")
    public ResponseEntity<CallResponse> hangupCall(@PathVariable UUID id) {
        return ResponseEntity.ok(callService.hangupCall(id));
    }

    @GetMapping("/agent/{agentId}")
    public ResponseEntity<Page<CallResponse>> getAgentActiveCalls(
            @PathVariable UUID agentId, Pageable pageable) {
        return ResponseEntity.ok(callService.getAgentActiveCalls(agentId, pageable));
    }

    @GetMapping("/active")
    public ResponseEntity<Page<CallResponse>> getActiveCalls(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            Pageable pageable) {
        if (start == null) start = LocalDateTime.now().minusHours(24);
        if (end == null) end = LocalDateTime.now();
        return ResponseEntity.ok(callService.getCallsByDateRange(Call.CallStatus.IN_PROGRESS, start, end, pageable));
    }

    @PostMapping("/{id}/transfer")
    public ResponseEntity<CallResponse> transferCall(@PathVariable UUID id,
                                                      @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(callService.transferCall(id, request.getTargetAgentId()));
    }

    @PostMapping("/{id}/mute")
    public ResponseEntity<ApiResponse<CallResponse>> muteCall(@PathVariable UUID id) {
        CallResponse response = callService.muteCall(id);
        return ResponseEntity.ok(ApiResponse.success("Call muted", response));
    }

    @PostMapping("/{id}/unmute")
    public ResponseEntity<ApiResponse<CallResponse>> unmuteCall(@PathVariable UUID id) {
        CallResponse response = callService.unmuteCall(id);
        return ResponseEntity.ok(ApiResponse.success("Call unmuted", response));
    }

    @PostMapping("/{id}/hold")
    public ResponseEntity<ApiResponse<CallResponse>> holdCall(@PathVariable UUID id) {
        CallResponse response = callService.holdCall(id);
        return ResponseEntity.ok(ApiResponse.success("Call on hold", response));
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<ApiResponse<CallResponse>> resumeCall(@PathVariable UUID id) {
        CallResponse response = callService.resumeCall(id);
        return ResponseEntity.ok(ApiResponse.success("Call resumed", response));
    }

    @PostMapping("/{id}/satisfaction")
    public ResponseEntity<ApiResponse<CallResponse>> submitSatisfaction(
            @PathVariable UUID id, @Valid @RequestBody SatisfactionRequest request) {
        CallResponse response = callService.submitSatisfaction(id, request.getScore(), request.getComment());
        return ResponseEntity.ok(ApiResponse.success("Satisfaction submitted", response));
    }

    @PostMapping("/{id}/send-survey")
    public ResponseEntity<ApiResponse<CallResponse>> sendSurvey(@PathVariable UUID id) {
        CallResponse response = callService.sendSurvey(id);
        return ResponseEntity.ok(ApiResponse.success("Survey sent", response));
    }

    @PostMapping("/{id}/evaluations")
    public ResponseEntity<CallEvaluationResponse> createEvaluation(
            @PathVariable UUID id, @Valid @RequestBody CallEvaluationRequest request) {
        UUID evaluatorId = null;
        String evaluatorName = null;
        CallEvaluationResponse response = evaluationService.createEvaluation(id, request, evaluatorId, evaluatorName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/evaluations")
    public ResponseEntity<Page<CallEvaluationResponse>> getEvaluations(
            @PathVariable UUID id, Pageable pageable) {
        return ResponseEntity.ok(evaluationService.getEvaluationsByCall(id, pageable));
    }
}
