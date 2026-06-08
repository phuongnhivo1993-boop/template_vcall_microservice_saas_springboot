package com.vcall.xr.webxr.dto;

import com.vcall.xr.webxr.domain.XrSession;

public class XrSessionMapper {

    public static XrSessionResponse toResponse(XrSession entity) {
        if (entity == null) return null;
        return XrSessionResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .tenantId(entity.getTenantId())
                .sceneId(entity.getSceneId())
                .deviceType(entity.getDeviceType())
                .deviceInfo(entity.getDeviceInfo())
                .startedAt(entity.getStartedAt())
                .endedAt(entity.getEndedAt())
                .durationSeconds(entity.getDurationSeconds())
                .gazeData(entity.getGazeData())
                .interactions(entity.getInteractions())
                .fpsAvg(entity.getFpsAvg())
                .loadTimeMs(entity.getLoadTimeMs())
                .build();
    }

    public static XrSession toEntity(XrSessionRequest request) {
        if (request == null) return null;
        XrSession session = new XrSession();
        session.setUserId(request.getUserId());
        session.setTenantId(request.getTenantId());
        session.setSceneId(request.getSceneId());
        session.setDeviceType(request.getDeviceType());
        session.setDeviceInfo(request.getDeviceInfo());
        return session;
    }
}
