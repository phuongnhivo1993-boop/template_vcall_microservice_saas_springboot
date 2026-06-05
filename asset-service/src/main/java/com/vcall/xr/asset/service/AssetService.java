package com.vcall.xr.asset.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.xr.asset.domain.Asset;
import com.vcall.xr.asset.domain.AssetType;
import com.vcall.xr.asset.domain.TranscodeStatus;
import com.vcall.xr.asset.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final MinioService minioService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String ASSET_EVENTS_TOPIC = "asset-events";

    @Transactional
    public Asset uploadAsset(MultipartFile file, String name, AssetType type, UUID tenantId,
                             Map<String, Object> metadata) {
        String objectKey = minioService.uploadFile(file, tenantId.toString(), type);

        Asset asset = Asset.builder()
                .tenantId(tenantId)
                .name(name)
                .type(type)
                .originalUrl(minioService.getBaseUrl() + objectKey)
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .transcodeStatus(TranscodeStatus.PENDING)
                .processingProgress(0)
                .metadata(metadata != null ? metadata : new HashMap<>())
                .build();

        asset = assetRepository.save(asset);
        log.info("Asset uploaded: id={}, name={}, type={}", asset.getId(), name, type);

        publishAssetEvent("ASSET_UPLOADED", asset);

        return asset;
    }

    @Transactional
    public Asset uploadVideo360(MultipartFile file, String name, UUID tenantId,
                                String videoType, String resolution, Integer durationSeconds) {
        Asset asset = uploadAsset(file, name, AssetType.VIDEO_360, tenantId, null);
        asset.setVideoType(videoType);
        asset.setResolution(resolution);
        asset.setDurationSeconds(durationSeconds);
        return assetRepository.save(asset);
    }

    @Transactional
    public Asset uploadModel3D(MultipartFile file, String name, UUID tenantId,
                               String modelFormat, boolean hasDracoCompression, boolean hasKtx2Textures) {
        Asset asset = uploadAsset(file, name, AssetType.MODEL_3D, tenantId, null);
        asset.setModelFormat(modelFormat);
        asset.setHasDracoCompression(hasDracoCompression);
        asset.setHasKtx2Textures(hasKtx2Textures);
        return assetRepository.save(asset);
    }

    @Transactional
    public Asset processAsset(UUID assetId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + assetId));

        asset.setTranscodeStatus(TranscodeStatus.PROCESSING);
        asset.setProcessingProgress(0);
        asset = assetRepository.save(asset);

        publishAssetEvent("ASSET_PROCESSING_STARTED", asset);

        log.info("Asset processing started: id={}", assetId);
        return asset;
    }

    @Transactional
    public Asset updateTranscodeStatus(UUID assetId, TranscodeStatus status, int progress) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + assetId));

        asset.setTranscodeStatus(status);
        asset.setProcessingProgress(progress);

        if (status == TranscodeStatus.COMPLETED) {
            asset.setProcessedUrl(asset.getOriginalUrl().replace("/original/", "/processed/"));
            asset.setHlsUrl(asset.getOriginalUrl().replace("/original/", "/hls/"));
            asset.setDashUrl(asset.getOriginalUrl().replace("/original/", "/dash/"));
            asset.setThumbnailUrl(asset.getOriginalUrl().replace("/original/", "/thumbnails/"));
        }

        asset = assetRepository.save(asset);
        publishAssetEvent("ASSET_TRANSCODE_UPDATED", asset);

        return asset;
    }

    @Transactional(readOnly = true)
    public Asset getAssetById(UUID id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Asset> getAssetsByTenant(UUID tenantId, Pageable pageable) {
        return assetRepository.findByTenantId(tenantId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Asset> getAssetsByTenantAndType(UUID tenantId, AssetType type, Pageable pageable) {
        return assetRepository.findByTenantIdAndType(tenantId, type, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Asset> searchAssets(Specification<Asset> spec, Pageable pageable) {
        return assetRepository.findAll(spec, pageable);
    }

    @Transactional
    public void deleteAsset(UUID id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + id));
        asset.setIsDeleted(true);
        assetRepository.save(asset);
        publishAssetEvent("ASSET_DELETED", asset);
    }

    @Transactional(readOnly = true)
    public String getStreamingUrl(UUID assetId, String quality) {
        Asset asset = getAssetById(assetId);

        if (asset.getType() == AssetType.VIDEO_360) {
            if ("hls".equalsIgnoreCase(quality) && asset.getHlsUrl() != null) {
                return asset.getHlsUrl();
            } else if ("dash".equalsIgnoreCase(quality) && asset.getDashUrl() != null) {
                return asset.getDashUrl();
            }
            return asset.getProcessedUrl() != null ? asset.getProcessedUrl() : asset.getOriginalUrl();
        }

        return asset.getProcessedUrl() != null ? asset.getProcessedUrl() : asset.getOriginalUrl();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAssetStats(UUID tenantId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", assetRepository.countByTenantIdAndType(tenantId, AssetType.VIDEO_360)
                + assetRepository.countByTenantIdAndType(tenantId, AssetType.IMAGE_360)
                + assetRepository.countByTenantIdAndType(tenantId, AssetType.MODEL_3D)
                + assetRepository.countByTenantIdAndType(tenantId, AssetType.AUDIO)
                + assetRepository.countByTenantIdAndType(tenantId, AssetType.PANORAMA));
        stats.put("video360", assetRepository.countByTenantIdAndType(tenantId, AssetType.VIDEO_360));
        stats.put("image360", assetRepository.countByTenantIdAndType(tenantId, AssetType.IMAGE_360));
        stats.put("model3d", assetRepository.countByTenantIdAndType(tenantId, AssetType.MODEL_3D));
        stats.put("audio", assetRepository.countByTenantIdAndType(tenantId, AssetType.AUDIO));
        stats.put("panorama", assetRepository.countByTenantIdAndType(tenantId, AssetType.PANORAMA));
        return stats;
    }

    private void publishAssetEvent(String eventType, Asset asset) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", eventType);
            event.put("assetId", asset.getId().toString());
            event.put("tenantId", asset.getTenantId().toString());
            event.put("name", asset.getName());
            event.put("type", asset.getType().name());
            event.put("timestamp", LocalDateTime.now().toString());

            kafkaTemplate.send(ASSET_EVENTS_TOPIC, asset.getId().toString(), event);
        } catch (Exception e) {
            log.error("Failed to publish asset event: {}", e.getMessage(), e);
        }
    }
}
