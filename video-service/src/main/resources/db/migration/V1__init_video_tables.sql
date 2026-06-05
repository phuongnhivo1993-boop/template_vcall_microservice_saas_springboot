-- V1__init_video_tables.sql
-- Initialize video service database schema

CREATE TABLE IF NOT EXISTS xr_video_job (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    asset_id VARCHAR(255) NOT NULL,
    input_url VARCHAR(1024) NOT NULL,
    output_url VARCHAR(1024),
    video_type VARCHAR(20) NOT NULL CHECK (video_type IN ('MONOSCOPIC', 'STEREOSCOPIC')),
    target_resolutions JSONB NOT NULL DEFAULT '["1920x1080"]',
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    progress INTEGER DEFAULT 0 CHECK (progress >= 0 AND progress <= 100),
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_xr_video_job_tenant_id ON xr_video_job(tenant_id);
CREATE INDEX idx_xr_video_job_asset_id ON xr_video_job(asset_id);
CREATE INDEX idx_xr_video_job_status ON xr_video_job(status);
CREATE INDEX idx_xr_video_job_created_at ON xr_video_job(created_at);
CREATE INDEX idx_xr_video_job_video_type ON xr_video_job(video_type);

CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_xr_video_job_modtime
    BEFORE UPDATE ON xr_video_job
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();
