CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE xr_scene (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    thumbnail_url VARCHAR(500),
    background_type VARCHAR(50),
    background_asset_id UUID,
    status VARCHAR(30) DEFAULT 'DRAFT',
    published_url VARCHAR(500),
    published_at TIMESTAMP,
    version INT DEFAULT 1,
    settings JSONB,
    view_count INT DEFAULT 0,
    avg_view_time_seconds FLOAT DEFAULT 0.0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_scene_tenant ON xr_scene(tenant_id);
CREATE INDEX idx_scene_status ON xr_scene(status);
CREATE INDEX idx_scene_type ON xr_scene(type);

CREATE TABLE xr_scene_node (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    scene_id UUID NOT NULL REFERENCES xr_scene(id) ON DELETE CASCADE,
    parent_id UUID,
    node_type VARCHAR(50) NOT NULL,
    name VARCHAR(255),
    position_x FLOAT DEFAULT 0.0,
    position_y FLOAT DEFAULT 0.0,
    position_z FLOAT DEFAULT 0.0,
    rotation_x FLOAT DEFAULT 0.0,
    rotation_y FLOAT DEFAULT 0.0,
    rotation_z FLOAT DEFAULT 0.0,
    scale_x FLOAT DEFAULT 1.0,
    scale_y FLOAT DEFAULT 1.0,
    scale_z FLOAT DEFAULT 1.0,
    content JSONB,
    visible BOOLEAN DEFAULT TRUE,
    interactive BOOLEAN DEFAULT FALSE,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_scene_node_scene ON xr_scene_node(scene_id);
CREATE INDEX idx_scene_node_parent ON xr_scene_node(parent_id);
CREATE INDEX idx_scene_node_type ON xr_scene_node(node_type);

CREATE TABLE xr_hotspot (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    scene_id UUID NOT NULL REFERENCES xr_scene(id) ON DELETE CASCADE,
    node_id UUID,
    hotspot_type VARCHAR(50) NOT NULL,
    latitude FLOAT,
    longitude FLOAT,
    title VARCHAR(255),
    description TEXT,
    icon_url VARCHAR(500),
    action_type VARCHAR(50),
    action_payload JSONB,
    style JSONB,
    animation VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_hotspot_scene ON xr_hotspot(scene_id);
CREATE INDEX idx_hotspot_node ON xr_hotspot(node_id);
CREATE INDEX idx_hotspot_type ON xr_hotspot(hotspot_type);
