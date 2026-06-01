CREATE TABLE email_accounts (
    id BIGSERIAL PRIMARY KEY,
    email_address VARCHAR(255) UNIQUE NOT NULL,
    display_name VARCHAR(255),
    provider VARCHAR(50) DEFAULT 'SMTP',
    smtp_host VARCHAR(255),
    smtp_port INT DEFAULT 587,
    smtp_username VARCHAR(255),
    smtp_password VARCHAR(255),
    smtp_ssl BOOLEAN DEFAULT TRUE,
    imap_host VARCHAR(255),
    imap_port INT DEFAULT 993,
    imap_username VARCHAR(255),
    imap_password VARCHAR(255),
    imap_ssl BOOLEAN DEFAULT TRUE,
    is_default BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE email_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    body_html TEXT,
    body_text TEXT,
    variables TEXT,
    category VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE email_messages (
    id UUID PRIMARY KEY,
    from_address VARCHAR(255) NOT NULL,
    to_addresses TEXT NOT NULL,
    cc_addresses TEXT,
    bcc_addresses TEXT,
    subject VARCHAR(255),
    body_html TEXT,
    body_text TEXT,
    status VARCHAR(20) DEFAULT 'DRAFT',
    priority VARCHAR(10) DEFAULT 'NORMAL',
    sent_at TIMESTAMP,
    opened_at TIMESTAMP,
    clicked_at TIMESTAMP,
    error_message TEXT,
    account_id BIGINT REFERENCES email_accounts(id),
    template_id BIGINT REFERENCES email_templates(id),
    external_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE email_attachments (
    id BIGSERIAL PRIMARY KEY,
    email_id UUID NOT NULL REFERENCES email_messages(id),
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(100),
    content_id VARCHAR(255),
    is_inline BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);
