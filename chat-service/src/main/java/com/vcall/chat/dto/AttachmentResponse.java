package com.vcall.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {
    private Long id;
    private Long messageId;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String mimeType;
}
