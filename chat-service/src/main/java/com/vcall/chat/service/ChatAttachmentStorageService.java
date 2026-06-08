package com.vcall.chat.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class ChatAttachmentStorageService {

    @Value("${chat.attachment.upload-dir:uploads/chat}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + uploadDir, e);
        }
    }

    public String storeFile(UUID messageId, MultipartFile file) {
        String fileName = messageId + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path targetPath = Paths.get(uploadDir).resolve(fileName);
        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return targetPath.toString();
        } catch (IOException e) {
            log.error("Failed to store file {}", fileName, e);
            throw new RuntimeException("Failed to store attachment file", e);
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            Files.deleteIfExists(Paths.get(fileUrl));
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", fileUrl, e);
        }
    }
}
