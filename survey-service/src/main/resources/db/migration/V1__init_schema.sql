CREATE TABLE IF NOT EXISTS surveys (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(500) NOT NULL,
    description     TEXT,
    config          TEXT,
    questions       TEXT NOT NULL,
    status          VARCHAR(20) DEFAULT 'DRAFT',
    locale          VARCHAR(10) DEFAULT 'vi',
    max_responses   INTEGER,
    response_count  INTEGER DEFAULT 0,
    start_date      TIMESTAMP,
    end_date        TIMESTAMP,
    tenant_id       VARCHAR(50),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted      BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS survey_responses (
    id              BIGSERIAL PRIMARY KEY,
    survey_id       BIGINT REFERENCES surveys(id),
    respondent_id   VARCHAR(100),
    respondent_email VARCHAR(255),
    answers         TEXT NOT NULL,
    score           INTEGER,
    feedback        TEXT,
    started_at      TIMESTAMP,
    completed_at    TIMESTAMP,
    ip_address      VARCHAR(45),
    user_agent      TEXT,
    tenant_id       VARCHAR(50),
    is_deleted      BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_surveys_status ON surveys(status);
CREATE INDEX IF NOT EXISTS idx_survey_responses_survey ON survey_responses(survey_id);
CREATE INDEX IF NOT EXISTS idx_survey_responses_completed ON survey_responses(completed_at DESC);
