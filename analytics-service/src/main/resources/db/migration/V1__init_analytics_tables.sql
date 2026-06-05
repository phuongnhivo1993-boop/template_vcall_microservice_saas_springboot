CREATE TABLE IF NOT EXISTS xr_analytics_event (
    id UUID,
    tenant_id String,
    user_id UUID,
    scene_id UUID,
    session_id UUID,
    event_type String,
    event_data String,
    device_type String,
    timestamp DateTime DEFAULT now()
) ENGINE = MergeTree()
ORDER BY (tenant_id, timestamp, event_type)
PARTITION BY toYYYYMM(timestamp);

CREATE TABLE IF NOT EXISTS xr_analytics_event_v1 (
    id UUID,
    tenant_id VARCHAR(50) NOT NULL,
    user_id UUID,
    scene_id UUID,
    session_id UUID,
    event_type VARCHAR(100) NOT NULL,
    event_data JSONB,
    device_type VARCHAR(50),
    timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id)
) ENGINE = MergeTree()
ORDER BY (tenant_id, timestamp, event_type)
PARTITION BY toYYYYMM(timestamp);
