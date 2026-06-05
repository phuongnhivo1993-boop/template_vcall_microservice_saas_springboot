CREATE TABLE xr_digital_twin (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(100) NOT NULL,
    bim_asset_id UUID,
    scene_id UUID,
    iot_endpoints JSONB,
    sync_interval_seconds INT DEFAULT 30,
    floors JSONB,
    rooms JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_digital_twin_tenant ON xr_digital_twin(tenant_id);
CREATE INDEX idx_digital_twin_type ON xr_digital_twin(type);
CREATE INDEX idx_digital_twin_bim ON xr_digital_twin(bim_asset_id);
CREATE INDEX idx_digital_twin_scene ON xr_digital_twin(scene_id);
