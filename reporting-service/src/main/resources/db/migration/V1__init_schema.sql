CREATE TABLE report_definitions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    category VARCHAR(50),
    report_type VARCHAR(50) NOT NULL,
    query_config TEXT,
    parameters_schema TEXT,
    output_format VARCHAR(20) DEFAULT 'CSV',
    is_scheduled BOOLEAN DEFAULT FALSE,
    cron_expression VARCHAR(100),
    is_system BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE report_executions (
    id BIGSERIAL PRIMARY KEY,
    report_id BIGINT NOT NULL REFERENCES report_definitions(id),
    parameters TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    row_count INT,
    file_path VARCHAR(500),
    file_size BIGINT,
    error_message TEXT,
    execution_time_ms BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE dashboard_widgets (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    widget_type VARCHAR(50) NOT NULL,
    config TEXT,
    data_query TEXT,
    position_x INT DEFAULT 0,
    position_y INT DEFAULT 0,
    width INT DEFAULT 2,
    height INT DEFAULT 2,
    refresh_interval INT DEFAULT 300,
    is_public BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE agent_performance_caches (
    id BIGSERIAL PRIMARY KEY,
    agent_id UUID NOT NULL,
    cache_date DATE NOT NULL,
    total_calls INT DEFAULT 0,
    answered_calls INT DEFAULT 0,
    missed_calls INT DEFAULT 0,
    avg_talk_duration BIGINT DEFAULT 0,
    total_talk_duration BIGINT DEFAULT 0,
    max_concurrent_calls INT DEFAULT 0,
    avg_after_call_work BIGINT DEFAULT 0,
    resolution_rate DECIMAL(5,2) DEFAULT 0,
    customer_satisfaction DECIMAL(3,2) DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE,
    UNIQUE(agent_id, cache_date)
);
