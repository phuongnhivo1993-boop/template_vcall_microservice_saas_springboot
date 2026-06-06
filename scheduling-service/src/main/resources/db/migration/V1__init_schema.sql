CREATE TABLE IF NOT EXISTS schedules (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    type            VARCHAR(50) NOT NULL,
    config          TEXT,
    cron_expression VARCHAR(100),
    timezone        VARCHAR(50) DEFAULT 'UTC',
    start_date      TIMESTAMP,
    end_date        TIMESTAMP,
    max_runs        INTEGER,
    run_count       INTEGER DEFAULT 0,
    last_run_at     TIMESTAMP,
    next_run_at     TIMESTAMP,
    status          VARCHAR(20) DEFAULT 'ACTIVE',
    tenant_id       VARCHAR(50),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted      BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS schedule_executions (
    id              BIGSERIAL PRIMARY KEY,
    schedule_id     BIGINT REFERENCES schedules(id),
    status          VARCHAR(20) DEFAULT 'PENDING',
    started_at      TIMESTAMP,
    completed_at    TIMESTAMP,
    result_data     TEXT,
    error_message   TEXT,
    execution_time  BIGINT,
    triggered_by    VARCHAR(20) DEFAULT 'SCHEDULED',
    tenant_id       VARCHAR(50),
    is_deleted      BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_schedules_next_run ON schedules(next_run_at, status);
CREATE INDEX IF NOT EXISTS idx_schedules_status ON schedules(status);
CREATE INDEX IF NOT EXISTS idx_schedule_executions_schedule ON schedule_executions(schedule_id);
