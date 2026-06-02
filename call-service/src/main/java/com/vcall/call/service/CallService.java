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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public List<CallResponse> getAgentActiveCalls(UUID agentId) {
        return callRepository.findByAgentIdAndStatus(agentId, Call.CallStatus.IN_PROGRESS).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CallResponse> getCallsByDateRange(Call.CallStatus status, LocalDateTime start, LocalDateTime end) {
        return callRepository.findByStatusAndStartTimeBetween(status, start, end).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
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
                .build();
    }
}
