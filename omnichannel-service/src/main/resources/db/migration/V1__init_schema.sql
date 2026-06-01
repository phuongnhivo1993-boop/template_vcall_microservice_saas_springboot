CREATE TABLE conversations (
    id UUID PRIMARY KEY,
    channel VARCHAR(20) NOT NULL,
    channel_conversation_id VARCHAR(255),
    customer_id UUID,
    agent_id UUID,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    subject VARCHAR(255),
    channel_data TEXT,
    started_at TIMESTAMP,
    ended_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES conversations(id),
    sender_id UUID,
    sender_type VARCHAR(20) NOT NULL,
    content TEXT,
    content_type VARCHAR(50) DEFAULT 'TEXT',
    channel_message_id VARCHAR(255),
    is_read BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE omnichannel_routing_rules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    channel VARCHAR(20),
    condition_expression TEXT,
    priority INT DEFAULT 0,
    destination_type VARCHAR(50),
    destination_id BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE channel_configs (
    id BIGSERIAL PRIMARY KEY,
    channel VARCHAR(20) UNIQUE NOT NULL,
    display_name VARCHAR(100),
    is_enabled BOOLEAN DEFAULT TRUE,
    config_json TEXT,
    api_key VARCHAR(255),
    api_secret VARCHAR(255),
    webhook_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);
