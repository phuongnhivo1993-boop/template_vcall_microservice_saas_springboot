package com.vcall.recording.service;

import com.vcall.recording.config.MinIOConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecordingStorageService {

    private final S3Client s3Client;
    private final MinIOConfig minIOConfig;

    public String uploadFile(String key, MultipartFile file) {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(minIOConfig.getBucket())
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();
            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return key;
        } catch (S3Exception | IOException e) {
            log.error("Failed to upload file to MinIO: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file to storage", e);
        }
    }

    public String uploadFile(String key, InputStream inputStream, long contentLength, String contentType) {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(minIOConfig.getBucket())
                    .key(key)
                    .contentType(contentType)
                    .contentLength(contentLength)
                    .build();
            s3Client.putObject(putRequest, RequestBody.fromInputStream(inputStream, contentLength));
            return key;
        } catch (S3Exception e) {
            log.error("Failed to upload file to MinIO: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file to storage", e);
        }
    }

    public ResponseBytes<software.amazon.awssdk.services.s3.model.GetObjectResponse> downloadFile(String key) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(minIOConfig.getBucket())
                    .key(key)
                    .build();
            return s3Client.getObjectAsBytes(getRequest);
        } catch (S3Exception e) {
            log.error("Failed to download file from MinIO: {}", e.getMessage());
            throw new RuntimeException("Failed to download file from storage", e);
        }
    }

    public URL getFileUrl(String key) {
        try {
            GetUrlRequest urlRequest = GetUrlRequest.builder()
                    .bucket(minIOConfig.getBucket())
                    .key(key)
                    .build();
            return s3Client.utilities().getUrl(urlRequest);
        } catch (S3Exception e) {
            log.error("Failed to get file URL from MinIO: {}", e.getMessage());
            throw new RuntimeException("Failed to get file URL from storage", e);
        }
    }

    public void deleteFile(String key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(minIOConfig.getBucket())
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteRequest);
        } catch (S3Exception e) {
            log.error("Failed to delete file from MinIO: {}", e.getMessage());
            throw new RuntimeException("Failed to delete file from storage", e);
        }
    }
}
