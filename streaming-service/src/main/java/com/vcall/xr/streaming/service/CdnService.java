package com.vcall.xr.streaming.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class CdnService {

    private final MinioStorageService storageService;

    @Value("${cdn.enabled:true}")
    private boolean cdnEnabled;

    @Value("${cdn.origin:https://stream.vcall.local}")
    private String cdnOrigin;

    @Value("${cdn.cache-ttl:86400}")
    private int cacheTtl;

    @Value("${cdn.cors-allowed-origins:*}")
    private List<String> corsAllowedOrigins;

    @Value("${cdn.cors-allowed-methods:GET,HEAD}")
    private List<String> corsAllowedMethods;

    private final ConcurrentHashMap<String, CdnUrlEntry> urlCache = new ConcurrentHashMap<>();

    public String getCdnUrl(UUID streamId, String path) {
        String cacheKey = streamId + "/" + path;

        CdnUrlEntry cached = urlCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return cached.getUrl();
        }

        String url;
        if (cdnEnabled) {
            url = buildCdnUrl(streamId, path);
        } else {
            url = storageService.getPublicUrl(cacheKey);
        }

        urlCache.put(cacheKey, new CdnUrlEntry(url, Instant.now().plus(Duration.ofSeconds(cacheTtl))));
        log.debug("Generated CDN URL for stream {}: {}", streamId, url);

        return url;
    }

    public CdnInfo getCdnInfo(UUID streamId) {
        CdnInfo info = new CdnInfo();
        info.setOrigin(cdnOrigin);
        info.setEnabled(cdnEnabled);
        info.setCacheTtl(cacheTtl);
        info.setCorsOrigins(corsAllowedOrigins);
        info.setCorsMethods(corsAllowedMethods);

        String manifestHls = getCdnUrl(streamId, "master.m3u8");
        String manifestDash = getCdnUrl(streamId, "manifest.mpd");

        info.setHlsUrl(manifestHls);
        info.setDashUrl(manifestDash);

        return info;
    }

    public String getManifestWithFallback(UUID streamId, String format) {
        String primaryPath;
        String fallbackPath;

        if ("HLS".equalsIgnoreCase(format)) {
            primaryPath = "master.m3u8";
            fallbackPath = "manifest.mpd";
        } else {
            primaryPath = "manifest.mpd";
            fallbackPath = "master.m3u8";
        }

        return getCdnUrl(streamId, primaryPath);
    }

    public CdnHeaders getCdnHeaders(UUID streamId) {
        CdnHeaders headers = new CdnHeaders();
        headers.setCacheControl("public, max-age=" + cacheTtl);
        headers.setAccessControlAllowOrigin(corsAllowedOrigins.get(0));
        headers.setAccessControlAllowMethods(String.join(", ", corsAllowedMethods));
        headers.setAccessControlAllowHeaders("*");
        headers.setAccessControlMaxAge(3600);
        headers.setContentType("application/octet-stream");

        return headers;
    }

    private String buildCdnUrl(UUID streamId, String path) {
        return String.format("%s/stream/%s/%s", cdnOrigin, streamId, path);
    }

    public void invalidateCache(UUID streamId) {
        urlCache.entrySet().removeIf(entry -> entry.getKey().startsWith(streamId.toString()));
        log.info("Invalidated CDN cache for stream {}", streamId);
    }

    public void invalidateAll() {
        urlCache.clear();
        log.info("Invalidated all CDN cache entries");
    }

    @Data
    public static class CdnInfo {
        private String origin;
        private boolean enabled;
        private int cacheTtl;
        private String hlsUrl;
        private String dashUrl;
        private List<String> corsOrigins;
        private List<String> corsMethods;
    }

    @Data
    public static class CdnHeaders {
        private String cacheControl;
        private String accessControlAllowOrigin;
        private String accessControlAllowMethods;
        private String accessControlAllowHeaders;
        private int accessControlMaxAge;
        private String contentType;
    }

    @Data
    @RequiredArgsConstructor
    private static class CdnUrlEntry {
        private final String url;
        private final Instant expiresAt;

        public boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
