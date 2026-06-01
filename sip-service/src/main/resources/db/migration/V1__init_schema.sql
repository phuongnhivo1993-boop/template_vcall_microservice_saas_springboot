CREATE TABLE sip_accounts (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    domain VARCHAR(255),
    realm VARCHAR(255),
    account_type VARCHAR(20) DEFAULT 'INTERNAL',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    max_channels INT DEFAULT 10,
    allow_registration BOOLEAN DEFAULT TRUE,
    tenant_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE sip_devices (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100),
    device_type VARCHAR(20),
    user_agent VARCHAR(255),
    ip_address VARCHAR(45),
    mac_address VARCHAR(17),
    firmware_version VARCHAR(50),
    sip_account_id BIGINT REFERENCES sip_accounts(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE sip_registrations (
    id BIGSERIAL PRIMARY KEY,
    sip_account_id BIGINT NOT NULL REFERENCES sip_accounts(id),
    contact_uri VARCHAR(255),
    user_agent VARCHAR(255),
    ip_address VARCHAR(45),
    port INT,
    transport VARCHAR(10) DEFAULT 'UDP',
    expires INT DEFAULT 3600,
    registered_at TIMESTAMP,
    last_refresh TIMESTAMP,
    status VARCHAR(20) DEFAULT 'REGISTERED',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);
