CREATE TABLE leads (
    id UUID PRIMARY KEY,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255),
    phone VARCHAR(20),
    company VARCHAR(255),
    title VARCHAR(255),
    source VARCHAR(50),
    status VARCHAR(50) DEFAULT 'NEW',
    score INT DEFAULT 0,
    assigned_to UUID,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE opportunities (
    id UUID PRIMARY KEY,
    lead_id UUID REFERENCES leads(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    value DECIMAL(18,2),
    currency VARCHAR(10) DEFAULT 'VND',
    stage VARCHAR(50) DEFAULT 'PROSPECTING',
    probability INT DEFAULT 0,
    expected_close_date DATE,
    assigned_to UUID,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE activities (
    id BIGSERIAL PRIMARY KEY,
    customer_id UUID,
    lead_id UUID,
    type VARCHAR(50) NOT NULL,
    subject VARCHAR(255),
    description TEXT,
    activity_date TIMESTAMP,
    duration INT,
    assigned_to UUID,
    result VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE customer_notes (
    id BIGSERIAL PRIMARY KEY,
    customer_id UUID,
    lead_id UUID,
    title VARCHAR(255),
    content TEXT,
    is_pinned BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);
