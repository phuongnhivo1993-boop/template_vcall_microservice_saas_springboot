package com.vcall.call.service;

import com.vcall.call.dto.IvrFlowRequest;
import com.vcall.call.dto.IvrFlowResponse;
import com.vcall.call.dto.IvrStepRequest;
import com.vcall.call.entity.IvrFlow;
import com.vcall.call.entity.IvrStep;
import com.vcall.call.repository.IvrFlowRepository;
import com.vcall.call.repository.IvrStepRepository;
import com.vcall.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IvrFlowService {

    private final IvrFlowRepository ivrFlowRepository;
    private final IvrStepRepository ivrStepRepository;

    @Transactional
    public IvrFlowResponse createFlow(IvrFlowRequest request) {
        IvrFlow flow = new IvrFlow();
        flow.setName(request.getName());
        flow.setDescription(request.getDescription());
        flow.setGreetingMessage(request.getGreetingMessage());
        flow.setFallbackDestination(request.getFallbackDestination());
        flow.setTimeout(request.getTimeout() != null ? request.getTimeout() : 30);
        flow = ivrFlowRepository.save(flow);
        return toResponse(flow);
    }

    @Transactional(readOnly = true)
    public IvrFlowResponse getFlow(Long id) {
        IvrFlow flow = ivrFlowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IVR flow not found with id: " + id));
        return toResponse(flow);
    }

    @Transactional(readOnly = true)
    public List<IvrFlowResponse> getAllFlows() {
        return ivrFlowRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public IvrFlowResponse updateFlow(Long id, IvrFlowRequest request) {
        IvrFlow flow = ivrFlowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IVR flow not found with id: " + id));
        flow.setName(request.getName());
        flow.setDescription(request.getDescription());
        flow.setGreetingMessage(request.getGreetingMessage());
        flow.setFallbackDestination(request.getFallbackDestination());
        flow.setTimeout(request.getTimeout() != null ? request.getTimeout() : 30);
        flow = ivrFlowRepository.save(flow);
        return toResponse(flow);
    }

    @Transactional
    public void deleteFlow(Long id) {
        IvrFlow flow = ivrFlowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IVR flow not found with id: " + id));
        ivrFlowRepository.delete(flow);
    }

    @Transactional
    public IvrStepRequest addStep(Long flowId, IvrStepRequest stepRequest) {
        IvrFlow flow = ivrFlowRepository.findById(flowId)
                .orElseThrow(() -> new ResourceNotFoundException("IVR flow not found with id: " + flowId));
        IvrStep step = new IvrStep();
        step.setIvrFlow(flow);
        step.setStepOrder(stepRequest.getStepOrder());
        step.setType(IvrStep.IvrStepType.valueOf(stepRequest.getType().toUpperCase()));
        step.setConfig(stepRequest.getConfig());
        step = ivrStepRepository.save(step);
        return toStepResponse(step);
    }

    @Transactional(readOnly = true)
    public List<IvrStepRequest> getSteps(Long flowId) {
        return ivrStepRepository.findByIvrFlowIdOrderByStepOrder(flowId).stream()
                .map(this::toStepResponse)
                .collect(Collectors.toList());
    }

    public String processIvrInput(Long flowId, String input) {
        IvrFlow flow = ivrFlowRepository.findById(flowId)
                .orElseThrow(() -> new ResourceNotFoundException("IVR flow not found with id: " + flowId));
        List<IvrStep> steps = ivrStepRepository.findByIvrFlowIdOrderByStepOrder(flowId);

        for (IvrStep step : steps) {
            switch (step.getType()) {
                case MENU:
                    String destination = evaluateMenu(step.getConfig(), input);
                    if (destination != null) return destination;
                    break;
                case INPUT:
                    return handleInput(step, input);
                case TRANSFER:
                    return step.getConfig();
                case HANGUP:
                    return "HANGUP";
                case PLAY_MESSAGE:
                default:
                    break;
            }
        }
        return flow.getFallbackDestination();
    }

    private String evaluateMenu(String config, String input) {
        return null;
    }

    private String handleInput(IvrStep step, String input) {
        return step.getConfig();
    }

    private IvrFlowResponse toResponse(IvrFlow flow) {
        List<IvrStepRequest> steps = ivrStepRepository.findByIvrFlowIdOrderByStepOrder(flow.getId()).stream()
                .map(this::toStepResponse)
                .collect(Collectors.toList());
        return IvrFlowResponse.builder()
                .id(flow.getId())
                .name(flow.getName())
                .description(flow.getDescription())
                .greetingMessage(flow.getGreetingMessage())
                .fallbackDestination(flow.getFallbackDestination())
                .timeout(flow.getTimeout())
                .steps(steps)
                .build();
    }

    private IvrStepRequest toStepResponse(IvrStep step) {
        return IvrStepRequest.builder()
                .stepOrder(step.getStepOrder())
                .type(step.getType().name())
                .config(step.getConfig())
                .build();
    }
}
