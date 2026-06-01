package com.vcall.omnichannel.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.omnichannel.dto.request.ChannelConfigRequest;
import com.vcall.omnichannel.dto.response.ChannelConfigResponse;
import com.vcall.omnichannel.entity.Conversation.Channel;
import com.vcall.omnichannel.service.ChannelConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/omnichannel/channels")
@RequiredArgsConstructor
public class ChannelConfigController {

    private final ChannelConfigService channelConfigService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChannelConfigResponse>>> getAll() {
        List<ChannelConfigResponse> responses = channelConfigService.getAll();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ChannelConfigResponse>> getById(@PathVariable Long id) {
        ChannelConfigResponse response = channelConfigService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/channel/{channel}")
    public ResponseEntity<ApiResponse<ChannelConfigResponse>> getByChannel(@PathVariable Channel channel) {
        ChannelConfigResponse response = channelConfigService.getByChannel(channel);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChannelConfigResponse>> create(@Valid @RequestBody ChannelConfigRequest request) {
        ChannelConfigResponse response = channelConfigService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Channel config created", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ChannelConfigResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ChannelConfigRequest request) {
        ChannelConfigResponse response = channelConfigService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Channel config updated", response));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<ChannelConfigResponse>> toggleEnabled(@PathVariable Long id) {
        ChannelConfigResponse response = channelConfigService.toggleEnabled(id);
        return ResponseEntity.ok(ApiResponse.success("Channel config toggled", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        channelConfigService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Channel config deleted", null));
    }
}
