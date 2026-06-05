CREATE TABLE xr_tenant (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    plan VARCHAR(50) NOT NULL DEFAULT 'FREE',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    max_scenes INT NOT NULL DEFAULT 5,
    max_storage_gb BIGINT NOT NULL DEFAULT 10,
    max_bandwidth_gb BIGINT NOT NULL DEFAULT 50,
    features JSONB DEFAULT '{}',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    tenant_id VARCHAR(50),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE xr_subscription (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES xr_tenant(id),
    plan VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    billing_cycle VARCHAR(50) NOT NULL DEFAULT 'MONTHLY',
    price_cents BIGINT NOT NULL DEFAULT 0,
    storage_used_bytes BIGINT NOT NULL DEFAULT 0,
    bandwidth_used_bytes BIGINT NOT NULL DEFAULT 0,
    scenes_count INT NOT NULL DEFAULT 0,
    users_count INT NOT NULL DEFAULT 0,
    current_period_start TIMESTAMP NOT NULL DEFAULT NOW(),
    current_period_end TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE xr_feature_flag (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES xr_tenant(id),
    feature_key VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    config JSONB DEFAULT '{}',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE,
    UNIQUE(tenant_id, feature_key)
);

CREATE INDEX idx_xr_subscription_tenant_id ON xr_subscription(tenant_id);
CREATE INDEX idx_xr_feature_flag_tenant_id ON xr_feature_flag(tenant_id);
CREATE INDEX idx_xr_feature_flag_feature_key ON xr_feature_flag(feature_key);
CREATE INDEX idx_xr_tenant_slug ON xr_tenant(slug);
