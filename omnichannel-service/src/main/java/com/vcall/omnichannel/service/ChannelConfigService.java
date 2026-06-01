package com.vcall.omnichannel.service;

import com.vcall.omnichannel.dto.request.ChannelConfigRequest;
import com.vcall.omnichannel.dto.response.ChannelConfigResponse;
import com.vcall.omnichannel.entity.ChannelConfig;
import com.vcall.omnichannel.entity.Conversation.Channel;
import com.vcall.omnichannel.repository.ChannelConfigRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChannelConfigService {

    private final ChannelConfigRepository channelConfigRepository;

    @Transactional(readOnly = true)
    public List<ChannelConfigResponse> getAll() {
        return channelConfigRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChannelConfigResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public ChannelConfigResponse getByChannel(Channel channel) {
        return channelConfigRepository.findByChannel(channel)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Channel config not found for channel: " + channel));
    }

    @Transactional
    public ChannelConfigResponse create(ChannelConfigRequest request) {
        ChannelConfig config = new ChannelConfig();
        config.setChannel(request.getChannel());
        config.setIsEnabled(request.getIsEnabled() != null ? request.getIsEnabled() : true);
        config.setConfig(request.getConfig());

        config = channelConfigRepository.save(config);
        return toResponse(config);
    }

    @Transactional
    public ChannelConfigResponse update(Long id, ChannelConfigRequest request) {
        ChannelConfig config = findById(id);
        config.setChannel(request.getChannel());
        config.setIsEnabled(request.getIsEnabled());
        config.setConfig(request.getConfig());

        config = channelConfigRepository.save(config);
        return toResponse(config);
    }

    @Transactional
    public ChannelConfigResponse toggleEnabled(Long id) {
        ChannelConfig config = findById(id);
        config.setIsEnabled(!Boolean.TRUE.equals(config.getIsEnabled()));

        config = channelConfigRepository.save(config);
        return toResponse(config);
    }

    @Transactional
    public void delete(Long id) {
        ChannelConfig config = findById(id);
        channelConfigRepository.delete(config);
    }

    private ChannelConfig findById(Long id) {
        return channelConfigRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ChannelConfig not found with id: " + id));
    }

    private ChannelConfigResponse toResponse(ChannelConfig config) {
        return ChannelConfigResponse.builder()
                .id(config.getId())
                .channel(config.getChannel())
                .isEnabled(config.getIsEnabled())
                .config(config.getConfig())
                .build();
    }
}
