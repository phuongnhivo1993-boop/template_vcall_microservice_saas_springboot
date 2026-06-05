package com.vcall.xr.bim.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Map;

@Service
@Slf4j
public class BimViewerService {

    private final MinioClient minioClient;
    private final IfcParserService ifcParserService;

    @Value("${minio.bucket:bim-cad-files}")
    private String bucket;

    public BimViewerService(@Value("${minio.endpoint}") String endpoint,
                            @Value("${minio.access-key}") String accessKey,
                            @Value("${minio.secret-key}") String secretKey,
                            IfcParserService ifcParserService) {
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        this.ifcParserService = ifcParserService;
    }

    public Map<String, Object> getModelMetadata(String objectName) {
        Map<String, Object> parsed = ifcParserService.parseIfcFile(objectName);
        return Map.of(
                "objectName", objectName,
                "fileSize", parsed.get("fileSize"),
                "format", parsed.get("format"),
                "entities", parsed.get("entities"),
                "spatialStructure", parsed.get("spatialStructure"),
                "products", parsed.get("products")
        );
    }

    public InputStream getModelStream(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build());
        } catch (Exception e) {
            log.error("Error getting model stream: {}", objectName, e);
            throw new RuntimeException("Failed to get model stream: " + e.getMessage());
        }
    }

    public Map<String, Object> getModelHierarchy(String objectName) {
        Map<String, Object> parsed = ifcParserService.parseIfcFile(objectName);
        return Map.of(
                "root", Map.of(
                        "type", "IfcProject",
                        "name", objectName,
                        "children", Map.of(
                                "site", Map.of("type", "IfcSite", "children", Map.of(
                                        "building", Map.of("type", "IfcBuilding")
                                ))
                        )
                ),
                "products", parsed.get("products")
        );
    }

    public Map<String, Object> getModelStatistics(String objectName) {
        Map<String, Object> parsed = ifcParserService.parseIfcFile(objectName);
        return Map.of(
                "fileName", objectName,
                "statistics", parsed.get("products"),
                "entities", parsed.get("entities")
        );
    }
}
