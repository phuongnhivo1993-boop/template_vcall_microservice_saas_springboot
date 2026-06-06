CREATE TABLE IF NOT EXISTS webhook_endpoints (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    url             VARCHAR(2048) NOT NULL,
    secret          VARCHAR(512),
    events          TEXT NOT NULL,
    status          VARCHAR(20) DEFAULT 'ACTIVE',
    retry_count     INTEGER DEFAULT 3,
    timeout_ms      INTEGER DEFAULT 5000,
    headers         TEXT,
    tenant_id       VARCHAR(50),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted      BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS webhook_delivery_logs (
    id              BIGSERIAL PRIMARY KEY,
    endpoint_id     BIGINT REFERENCES webhook_endpoints(id),
    event_type      VARCHAR(255) NOT NULL,
    payload         TEXT,
    status          VARCHAR(20) DEFAULT 'PENDING',
    request_headers TEXT,
    response_status INTEGER,
    response_body   TEXT,
    attempt         INTEGER DEFAULT 1,
    error_message   TEXT,
    duration_ms     BIGINT,
    delivered_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tenant_id       VARCHAR(50),
    is_deleted      BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_webhook_endpoints_status ON webhook_endpoints(status);
CREATE INDEX IF NOT EXISTS idx_webhook_delivery_logs_endpoint ON webhook_delivery_logs(endpoint_id);
CREATE INDEX IF NOT EXISTS idx_webhook_delivery_logs_delivered ON webhook_delivery_logs(delivered_at DESC);
