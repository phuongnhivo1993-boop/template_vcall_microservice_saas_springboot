CREATE TABLE xr_session (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    user_id UUID NOT NULL,
    scene_id UUID NOT NULL,
    device_type VARCHAR(50) NOT NULL,
    device_info JSONB,
    started_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ended_at TIMESTAMP,
    duration_seconds BIGINT,
    gaze_data JSONB,
    interactions JSONB,
    fps_avg DOUBLE PRECISION,
    load_time_ms BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_xr_session_tenant_id ON xr_session(tenant_id);
CREATE INDEX idx_xr_session_user_id ON xr_session(user_id);
CREATE INDEX idx_xr_session_scene_id ON xr_session(scene_id);
CREATE INDEX idx_xr_session_device_type ON xr_session(device_type);
CREATE INDEX idx_xr_session_started_at ON xr_session(started_at);
