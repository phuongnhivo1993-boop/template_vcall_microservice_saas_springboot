CREATE TABLE calls (
    id UUID PRIMARY KEY,
    call_id VARCHAR(255) UNIQUE,
    caller_number VARCHAR(50),
    callee_number VARCHAR(50),
    caller_name VARCHAR(255),
    direction VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'RINGING',
    start_time TIMESTAMP,
    answer_time TIMESTAMP,
    end_time TIMESTAMP,
    duration BIGINT,
    agent_id UUID,
    queue_id BIGINT,
    ivr_flow_id BIGINT,
    hangup_cause VARCHAR(100),
    recording_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE call_queues (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    strategy VARCHAR(50) DEFAULT 'RING_ALL',
    max_wait_time INT DEFAULT 60,
    max_queue_size INT DEFAULT 100,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE call_queue_members (
    id BIGSERIAL PRIMARY KEY,
    queue_id BIGINT NOT NULL REFERENCES call_queues(id),
    agent_id UUID NOT NULL,
    priority INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE,
    UNIQUE(queue_id, agent_id)
);

CREATE TABLE ivr_flows (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    greeting_message TEXT,
    fallback_destination VARCHAR(255),
    timeout INT DEFAULT 30,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE ivr_steps (
    id BIGSERIAL PRIMARY KEY,
    ivr_flow_id BIGINT NOT NULL REFERENCES ivr_flows(id),
    step_order INT NOT NULL,
    type VARCHAR(50) NOT NULL,
    config TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE call_routing_rules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    priority INT DEFAULT 0,
    condition TEXT,
    destination VARCHAR(50),
    destination_id BIGINT,
    time_profile VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);
