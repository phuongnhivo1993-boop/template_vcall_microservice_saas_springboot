CREATE TABLE extensions (
    id BIGSERIAL PRIMARY KEY,
    extension_number VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    email VARCHAR(255),
    mobile_number VARCHAR(20),
    department VARCHAR(100),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    voicemail_enabled BOOLEAN DEFAULT TRUE,
    call_recording BOOLEAN DEFAULT FALSE,
    max_concurrent_calls INT DEFAULT 3,
    sip_account_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE ring_groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    strategy VARCHAR(50) DEFAULT 'RING_ALL',
    timeout INT DEFAULT 30,
    destination_on_timeout VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE ring_group_members (
    id BIGSERIAL PRIMARY KEY,
    ring_group_id BIGINT NOT NULL REFERENCES ring_groups(id),
    extension_id BIGINT NOT NULL REFERENCES extensions(id),
    priority INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE,
    UNIQUE(ring_group_id, extension_id)
);

CREATE TABLE pbx_queues (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    strategy VARCHAR(50) DEFAULT 'FIFO',
    max_wait_time INT DEFAULT 60,
    max_queue_size INT DEFAULT 100,
    announce_position BOOLEAN DEFAULT TRUE,
    announce_hold_time BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE pbx_queue_members (
    id BIGSERIAL PRIMARY KEY,
    queue_id BIGINT NOT NULL REFERENCES pbx_queues(id),
    extension_id BIGINT NOT NULL REFERENCES extensions(id),
    priority INT DEFAULT 0,
    penalty INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE,
    UNIQUE(queue_id, extension_id)
);
