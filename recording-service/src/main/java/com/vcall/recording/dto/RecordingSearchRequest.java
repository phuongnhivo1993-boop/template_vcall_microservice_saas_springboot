package com.vcall.recording.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordingSearchRequest {

    private UUID agentId;
    private String customerNumber;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String format;
}
