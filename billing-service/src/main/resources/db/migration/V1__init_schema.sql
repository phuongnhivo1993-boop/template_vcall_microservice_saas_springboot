CREATE TABLE pricing_plans (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    plan_type VARCHAR(50) NOT NULL,
    price DECIMAL(18,2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'VND',
    billing_cycle VARCHAR(20) DEFAULT 'MONTHLY',
    max_users INT,
    max_agents INT,
    max_calls INT,
    features TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE subscriptions (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    plan_id BIGINT NOT NULL REFERENCES pricing_plans(id),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    start_date DATE NOT NULL,
    end_date DATE,
    auto_renew BOOLEAN DEFAULT TRUE,
    trial_end_date DATE,
    cancelled_at TIMESTAMP,
    cancellation_reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE usage_records (
    id BIGSERIAL PRIMARY KEY,
    tenant_id UUID NOT NULL,
    subscription_id UUID REFERENCES subscriptions(id),
    usage_type VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    unit VARCHAR(20),
    recorded_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE invoices (
    id UUID PRIMARY KEY,
    invoice_number VARCHAR(50) UNIQUE NOT NULL,
    tenant_id UUID NOT NULL,
    subscription_id UUID REFERENCES subscriptions(id),
    status VARCHAR(20) DEFAULT 'DRAFT',
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    paid_at TIMESTAMP,
    subtotal DECIMAL(18,2) NOT NULL,
    tax DECIMAL(18,2) DEFAULT 0,
    discount DECIMAL(18,2) DEFAULT 0,
    total DECIMAL(18,2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'VND',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE invoice_items (
    id BIGSERIAL PRIMARY KEY,
    invoice_id UUID NOT NULL REFERENCES invoices(id),
    description VARCHAR(255) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(18,2) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);
