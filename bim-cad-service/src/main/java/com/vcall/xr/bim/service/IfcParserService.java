package com.vcall.xr.bim.service;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@Slf4j
public class IfcParserService {

    private final MinioClient minioClient;

    @Value("${minio.bucket:bim-cad-files}")
    private String bucket;

    public IfcParserService(@Value("${minio.endpoint}") String endpoint,
                            @Value("${minio.access-key}") String accessKey,
                            @Value("${minio.secret-key}") String secretKey) {
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @PostConstruct
    public void init() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Created bucket: {}", bucket);
            }
        } catch (Exception e) {
            log.warn("Could not initialize MinIO bucket: {}", e.getMessage());
        }
    }

    public Map<String, Object> parseIfcFile(String objectName) {
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build());

            byte[] bytes = stream.readAllBytes();
            String ifcContent = new String(bytes, StandardCharsets.UTF_8);

            Map<String, Object> result = Map.of(
                    "fileName", objectName,
                    "fileSize", bytes.length,
                    "format", "IFC",
                    "entities", extractIfcEntities(ifcContent),
                    "spatialStructure", extractSpatialStructure(ifcContent),
                    "products", extractProducts(ifcContent)
            );

            stream.close();
            return result;
        } catch (Exception e) {
            log.error("Error parsing IFC file: {}", objectName, e);
            throw new RuntimeException("Failed to parse IFC file: " + e.getMessage());
        }
    }

    public boolean fileExists(String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String uploadIfcFile(String objectName, InputStream inputStream, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(inputStream, -1, 10485760)
                            .contentType(contentType)
                            .build());
            log.info("Uploaded IFC file: {}", objectName);
            return objectName;
        } catch (Exception e) {
            log.error("Error uploading IFC file: {}", objectName, e);
            throw new RuntimeException("Failed to upload IFC file: " + e.getMessage());
        }
    }

    private Map<String, Object> extractIfcEntities(String ifcContent) {
        return Map.of(
                "totalLines", ifcContent.lines().count(),
                "hasIfcProject", ifcContent.contains("IFCPROJECT"),
                "hasIfcSite", ifcContent.contains("IFCSITE"),
                "hasIfcBuilding", ifcContent.contains("IFCBUILDING"),
                "hasIfcBuildingStorey", ifcContent.contains("IFCBUILDINGSTOREY")
        );
    }

    private Map<String, Object> extractSpatialStructure(String ifcContent) {
        return Map.of(
                "hasSpatialRoot", ifcContent.contains("IFCRELAGGREGATES"),
                "hasGeometry", ifcContent.contains("IFCPRODUCTDEFINITIONSHAPE"),
                "hasMaterials", ifcContent.contains("IFCMATERIAL")
        );
    }

    private Map<String, Object> extractProducts(String ifcContent) {
        long wallCount = ifcContent.lines().filter(l -> l.contains("IFCWALL")).count();
        long doorCount = ifcContent.lines().filter(l -> l.contains("IFCDOOR")).count();
        long windowCount = ifcContent.lines().filter(l -> l.contains("IFCWINDOW")).count();
        long slabCount = ifcContent.lines().filter(l -> l.contains("IFCSLAB")).count();

        return Map.of(
                "walls", wallCount,
                "doors", doorCount,
                "windows", windowCount,
                "slabs", slabCount
        );
    }
}
