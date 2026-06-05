CREATE TABLE xr_asset (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    original_url VARCHAR(1000),
    processed_url VARCHAR(1000),
    thumbnail_url VARCHAR(1000),
    mime_type VARCHAR(100),
    file_size BIGINT,
    video_type VARCHAR(50),
    resolution VARCHAR(20),
    duration_seconds INT,
    model_format VARCHAR(20),
    has_draco_compression BOOLEAN DEFAULT FALSE,
    has_ktx2_textures BOOLEAN DEFAULT FALSE,
    transcode_status VARCHAR(50) DEFAULT 'PENDING',
    processing_progress INT DEFAULT 0,
    hls_url VARCHAR(1000),
    dash_url VARCHAR(1000),
    variants JSONB,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_xr_asset_tenant ON xr_asset(tenant_id);
CREATE INDEX idx_xr_asset_type ON xr_asset(type);
CREATE INDEX idx_xr_asset_transcode_status ON xr_asset(transcode_status);
CREATE INDEX idx_xr_asset_created_at ON xr_asset(created_at DESC);
