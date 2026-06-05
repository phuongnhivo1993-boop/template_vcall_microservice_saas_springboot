CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE xr_floor (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    level INT NOT NULL DEFAULT 0,
    building_id UUID NOT NULL,
    floor_plan_asset_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE xr_room (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    floor_id UUID NOT NULL REFERENCES xr_floor(id),
    name VARCHAR(255) NOT NULL,
    boundaries GEOMETRY(Polygon, 4326),
    center_point GEOMETRY(Point, 4326),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE xr_path (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    floor_id UUID NOT NULL REFERENCES xr_floor(id),
    path GEOMETRY(LineString, 4326),
    floor_level INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_floor_tenant ON xr_floor(tenant_id);
CREATE INDEX idx_floor_building ON xr_floor(building_id);
CREATE INDEX idx_floor_level ON xr_floor(level);
CREATE INDEX idx_room_floor ON xr_room(floor_id);
CREATE INDEX idx_room_boundaries ON xr_room USING GIST(boundaries);
CREATE INDEX idx_room_center ON xr_room USING GIST(center_point);
CREATE INDEX idx_path_floor ON xr_path(floor_id);
CREATE INDEX idx_path_geom ON xr_path USING GIST(path);
