CREATE TABLE IF NOT EXISTS knowledge_articles (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(500) NOT NULL,
    content         TEXT NOT NULL,
    category        VARCHAR(100),
    tags            TEXT,
    views           INTEGER DEFAULT 0,
    helpful_count   INTEGER DEFAULT 0,
    not_helpful_count INTEGER DEFAULT 0,
    status          VARCHAR(20) DEFAULT 'PUBLISHED',
    locale          VARCHAR(10) DEFAULT 'vi',
    tenant_id       VARCHAR(50),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted      BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS knowledge_categories (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    parent_id       BIGINT REFERENCES knowledge_categories(id),
    sort_order      INTEGER DEFAULT 0,
    tenant_id       VARCHAR(50),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted      BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_knowledge_articles_category ON knowledge_articles(category);
CREATE INDEX IF NOT EXISTS idx_knowledge_articles_status ON knowledge_articles(status);
CREATE INDEX IF NOT EXISTS idx_knowledge_articles_search ON knowledge_articles USING gin(to_tsvector('simple', title || ' ' || content));
