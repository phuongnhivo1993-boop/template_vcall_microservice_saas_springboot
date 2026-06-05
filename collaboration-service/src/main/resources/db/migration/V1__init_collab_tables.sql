CREATE TABLE xr_collaboration_room (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    scene_id UUID,
    name VARCHAR(255) NOT NULL,
    max_participants INT DEFAULT 10,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    host_user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE xr_collaboration_participant (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id UUID NOT NULL REFERENCES xr_collaboration_room(id),
    user_id UUID NOT NULL,
    avatar_config JSONB,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    left_at TIMESTAMP,
    is_muted BOOLEAN DEFAULT TRUE,
    is_screen_sharing BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_collab_room_tenant ON xr_collaboration_room(tenant_id);
CREATE INDEX idx_collab_room_status ON xr_collaboration_room(status);
CREATE INDEX idx_collab_room_host ON xr_collaboration_room(host_user_id);
CREATE INDEX idx_collab_participant_room ON xr_collaboration_participant(room_id);
CREATE INDEX idx_collab_participant_user ON xr_collaboration_participant(user_id);
