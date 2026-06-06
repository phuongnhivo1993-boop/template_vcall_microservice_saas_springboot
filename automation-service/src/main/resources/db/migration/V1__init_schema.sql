CREATE TABLE IF NOT EXISTS automation_rules (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    trigger_type    VARCHAR(50) NOT NULL,
    trigger_config  TEXT,
    action_type     VARCHAR(50) NOT NULL,
    action_config   TEXT,
    conditions      TEXT,
    priority        INTEGER DEFAULT 0,
    enabled         BOOLEAN DEFAULT TRUE,
    tenant_id       VARCHAR(50),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted      BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS automation_execution_logs (
    id              BIGSERIAL PRIMARY KEY,
    rule_id         BIGINT REFERENCES automation_rules(id),
    trigger_event   VARCHAR(255),
    status          VARCHAR(20) DEFAULT 'PENDING',
    input_data      TEXT,
    output_data     TEXT,
    error_message   TEXT,
    execution_time  BIGINT,
    executed_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tenant_id       VARCHAR(50),
    is_deleted      BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_automation_rules_enabled ON automation_rules(enabled);
CREATE INDEX IF NOT EXISTS idx_automation_execution_logs_rule ON automation_execution_logs(rule_id);
CREATE INDEX IF NOT EXISTS idx_automation_execution_logs_executed ON automation_execution_logs(executed_at DESC);
