package com.vcall.recording.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordingResponse {

    private UUID id;
    private UUID callId;
    private String fileName;
    private Long fileSize;
    private Long duration;
    private String format;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private UUID agentId;
    private String customerNumber;
    private String downloadUrl;
}
