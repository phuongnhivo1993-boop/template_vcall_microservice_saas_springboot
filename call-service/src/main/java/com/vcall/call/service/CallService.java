package com.vcall.call.service;

import com.vcall.call.dto.CallRequest;
import com.vcall.call.dto.CallResponse;
import com.vcall.call.dto.CallStatusRequest;
import com.vcall.call.entity.Call;
import com.vcall.call.entity.Call.CallStatus;
import com.vcall.call.kafka.CallEventPublisher;
import com.vcall.call.repository.CallRepository;
import com.vcall.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class CallService {

    private final CallRepository callRepository;
    private final CallEventPublisher eventPublisher;

    @Transactional
    public CallResponse createCall(CallRequest request) {
        Call call = new Call();
        call.setCallId(UUID.randomUUID().toString());
        call.setCallerNumber(request.getCallerNumber());
        call.setCalleeNumber(request.getCalleeNumber());
        call.setCallerName(request.getCallerName());
        call.setDirection(Call.CallDirection.valueOf(request.getDirection().toUpperCase()));
        call.setStatus(Call.CallStatus.RINGING);
        call.setStartTime(LocalDateTime.now());
        call.setQueueId(request.getQueueId());
        call.setIvrFlowId(request.getIvrFlowId());
        call = callRepository.save(call);

        eventPublisher.publishCallStarted(call);
        return toResponse(call);
    }

    @Transactional
    public CallResponse updateCallStatus(UUID id, CallStatusRequest request) {
        Call call = callRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Call not found with id: " + id));

        Call.CallStatus newStatus = Call.CallStatus.valueOf(request.getStatus().toUpperCase());
        call.setStatus(newStatus);

        switch (newStatus) {
            case IN_PROGRESS:
                call.setAnswerTime(LocalDateTime.now());
                if (request.getAgentId() != null) {
                    call.setAgentId(request.getAgentId());
                }
                eventPublisher.publishCallAnswered(call);
                break;
            case COMPLETED:
            case FAILED:
            case BUSY:
            case NO_ANSWER:
                call.setEndTime(LocalDateTime.now());
                if (call.getAnswerTime() != null) {
                    call.setDuration(java.time.Duration.between(call.getAnswerTime(), call.getEndTime()).getSeconds());
                }
                call.setHangupCause(request.getHangupCause());
                eventPublisher.publishCallEnded(call);
                break;
            default:
                break;
        }

        call = callRepository.save(call);
        return toResponse(call);
    }

    @Transactional(readOnly = true)
    public CallResponse getCall(UUID id) {
        Call call = callRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Call not found with id: " + id));
        return toResponse(call);
    }

    @Transactional(readOnly = true)
    public Page<CallResponse> getAgentActiveCalls(UUID agentId, Pageable pageable) {
        return callRepository.findByAgentIdAndStatus(agentId, Call.CallStatus.IN_PROGRESS, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CallResponse> getCallsByDateRange(Call.CallStatus status, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return callRepository.findByStatusAndStartTimeBetween(status, start, end, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public CallResponse hangupCall(UUID id) {
        Call call = callRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Call not found with id: " + id));
        call.setStatus(Call.CallStatus.COMPLETED);
        call.setEndTime(LocalDateTime.now());
        if (call.getAnswerTime() != null) {
            call.setDuration(java.time.Duration.between(call.getAnswerTime(), call.getEndTime()).getSeconds());
        }
        call.setHangupCause("USER_HANGUP");
        call = callRepository.save(call);
        eventPublisher.publishCallEnded(call);
        return toResponse(call);
    }

    @Transactional
    public CallResponse transferCall(UUID id, UUID targetAgentId) {
        Call call = callRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Call not found with id: " + id));
        call.setAgentId(targetAgentId);
        call = callRepository.save(call);
        eventPublisher.publishEvent("CALL_TRANSFERRED", call);
        return toResponse(call);
    }

    @Transactional
    public CallResponse muteCall(UUID id) {
        Call call = callRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Call not found with id: " + id));
        call.setStatus(CallStatus.IN_PROGRESS);
        call = callRepository.save(call);
        eventPublisher.publishEvent("CALL_MUTED", call);
        return toResponse(call);
    }

    @Transactional
    public CallResponse unmuteCall(UUID id) {
        Call call = callRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Call not found with id: " + id));
        call.setStatus(CallStatus.IN_PROGRESS);
        call = callRepository.save(call);
        eventPublisher.publishEvent("CALL_UNMUTED", call);
        return toResponse(call);
    }

    @Transactional
    public CallResponse holdCall(UUID id) {
        Call call = callRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Call not found with id: " + id));
        call.setStatus(CallStatus.ON_HOLD);
        call = callRepository.save(call);
        eventPublisher.publishEvent("CALL_HELD", call);
        return toResponse(call);
    }

    @Transactional
    public CallResponse resumeCall(UUID id) {
        Call call = callRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Call not found with id: " + id));
        call.setStatus(CallStatus.IN_PROGRESS);
        call = callRepository.save(call);
        eventPublisher.publishEvent("CALL_RESUMED", call);
        return toResponse(call);
    }

    @Transactional
    public CallResponse submitSatisfaction(UUID id, Integer score, String comment) {
        Call call = callRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Call not found with id: " + id));
        call.setSatisfactionScore(score);
        call.setSatisfactionComment(comment);
        call.setSatisfactionSurveyedAt(LocalDateTime.now());
        call = callRepository.save(call);
        eventPublisher.publishEvent("SATISFACTION_SUBMITTED", call);
        return toResponse(call);
    }

    @Transactional
    public CallResponse sendSurvey(UUID id) {
        Call call = callRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Call not found with id: " + id));
        call.setSatisfactionSurveySent(true);
        call = callRepository.save(call);
        eventPublisher.publishEvent("SURVEY_SENT", call);
        return toResponse(call);
    }

    public Map<String, Object> getCallStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCalls", callRepository.count());
        stats.put("activeCalls", callRepository.countByStatus(Call.CallStatus.IN_PROGRESS));
        stats.put("ringingCalls", callRepository.countByStatus(Call.CallStatus.RINGING));
        stats.put("completedCalls", callRepository.countByStatus(Call.CallStatus.COMPLETED));
        stats.put("onHoldCalls", callRepository.countByStatus(Call.CallStatus.ON_HOLD));
        return stats;
    }

    public Page<CallResponse> searchCalls(String status, String direction, UUID agentId,
                                           LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        if (startDate == null) startDate = LocalDateTime.now().minusDays(30);
        if (endDate == null) endDate = LocalDateTime.now();
        if (status != null) {
            return callRepository.findByStatusAndStartTimeBetween(
                    Call.CallStatus.valueOf(status.toUpperCase()), startDate, endDate, pageable).map(this::toResponse);
        }
        return callRepository.findAll(pageable).map(this::toResponse);
    }

    public List<CallResponse> exportAll() {
        return callRepository.findAll().stream().map(this::toResponse).toList();
    }

    private CallResponse toResponse(Call call) {
        return CallResponse.builder()
                .id(call.getId())
                .callId(call.getCallId())
                .callerNumber(call.getCallerNumber())
                .calleeNumber(call.getCalleeNumber())
                .callerName(call.getCallerName())
                .direction(call.getDirection().name())
                .status(call.getStatus().name())
                .startTime(call.getStartTime())
                .answerTime(call.getAnswerTime())
                .endTime(call.getEndTime())
                .duration(call.getDuration())
                .agentId(call.getAgentId())
                .queueId(call.getQueueId())
                .ivrFlowId(call.getIvrFlowId())
                .recordingId(call.getRecordingId())
                .satisfactionScore(call.getSatisfactionScore())
                .satisfactionComment(call.getSatisfactionComment())
                .satisfactionSurveyedAt(call.getSatisfactionSurveyedAt())
                .satisfactionSurveySent(call.getSatisfactionSurveySent())
                .build();
    }
}
