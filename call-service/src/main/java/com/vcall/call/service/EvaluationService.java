package com.vcall.call.service;

import com.vcall.call.dto.CallEvaluationRequest;
import com.vcall.call.dto.CallEvaluationResponse;
import com.vcall.call.entity.Call;
import com.vcall.call.entity.CallEvaluation;
import com.vcall.call.repository.CallEvaluationRepository;
import com.vcall.call.repository.CallRepository;
import com.vcall.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EvaluationService {

    private final CallEvaluationRepository evaluationRepository;
    private final CallRepository callRepository;

    @Transactional(readOnly = true)
    public Page<CallEvaluationResponse> getEvaluationsByCall(UUID callId, Pageable pageable) {
        return evaluationRepository.findByCallId(callId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CallEvaluationResponse> getEvaluationsByEvaluator(UUID evaluatorId, Pageable pageable) {
        return evaluationRepository.findByEvaluatorId(evaluatorId, pageable).map(this::toResponse);
    }

    public CallEvaluationResponse createEvaluation(UUID callId, CallEvaluationRequest request, UUID evaluatorId, String evaluatorName) {
        Call call = callRepository.findById(callId)
                .orElseThrow(() -> new ResourceNotFoundException("Call not found: " + callId));

        CallEvaluation evaluation = CallEvaluation.builder()
                .callId(callId)
                .evaluatorId(evaluatorId)
                .evaluatorName(evaluatorName)
                .score(request.getScore())
                .greetingScore(request.getGreetingScore())
                .knowledgeScore(request.getKnowledgeScore())
                .resolutionScore(request.getResolutionScore())
                .communicationScore(request.getCommunicationScore())
                .empathyScore(request.getEmpathyScore())
                .complianceScore(request.getComplianceScore())
                .comments(request.getComments())
                .strengths(request.getStrengths())
                .improvements(request.getImprovements())
                .status("SUBMITTED")
                .evaluationDate(LocalDateTime.now())
                .build();

        evaluationRepository.save(evaluation);
        return toResponse(evaluation);
    }

    private CallEvaluationResponse toResponse(CallEvaluation eval) {
        return CallEvaluationResponse.builder()
                .id(eval.getId())
                .callId(eval.getCallId())
                .evaluatorId(eval.getEvaluatorId())
                .evaluatorName(eval.getEvaluatorName())
                .score(eval.getScore())
                .maxScore(eval.getMaxScore())
                .greetingScore(eval.getGreetingScore())
                .knowledgeScore(eval.getKnowledgeScore())
                .resolutionScore(eval.getResolutionScore())
                .communicationScore(eval.getCommunicationScore())
                .empathyScore(eval.getEmpathyScore())
                .complianceScore(eval.getComplianceScore())
                .comments(eval.getComments())
                .strengths(eval.getStrengths())
                .improvements(eval.getImprovements())
                .status(eval.getStatus())
                .evaluationDate(eval.getEvaluationDate())
                .createdAt(eval.getCreatedAt())
                .build();
    }
}
