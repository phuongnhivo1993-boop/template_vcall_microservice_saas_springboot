CREATE TABLE recordings (
    id UUID PRIMARY KEY,
    call_id UUID,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    duration BIGINT,
    format VARCHAR(20) DEFAULT 'WAV',
    status VARCHAR(20) DEFAULT 'PROCESSING',
    agent_id UUID,
    customer_id UUID,
    source VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE recording_metadata (
    id BIGSERIAL PRIMARY KEY,
    recording_id UUID NOT NULL REFERENCES recordings(id),
    meta_key VARCHAR(100) NOT NULL,
    meta_value TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE,
    UNIQUE(recording_id, meta_key)
);

CREATE TABLE retention_policies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    retention_days INT NOT NULL,
    storage_class VARCHAR(50) DEFAULT 'STANDARD',
    applies_to VARCHAR(50) DEFAULT 'ALL',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);
