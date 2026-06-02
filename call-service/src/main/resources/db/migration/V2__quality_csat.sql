ALTER TABLE calls ADD COLUMN satisfaction_score INTEGER;
ALTER TABLE calls ADD COLUMN satisfaction_comment TEXT;
ALTER TABLE calls ADD COLUMN satisfaction_surveyed_at TIMESTAMP;
ALTER TABLE calls ADD COLUMN satisfaction_survey_sent BOOLEAN DEFAULT FALSE;

CREATE TABLE call_evaluations (
    id UUID PRIMARY KEY,
    call_id UUID NOT NULL REFERENCES calls(id),
    evaluator_id UUID,
    evaluator_name VARCHAR(255),
    score INTEGER NOT NULL,
    max_score INTEGER NOT NULL DEFAULT 100,
    greeting_score INTEGER DEFAULT 0,
    knowledge_score INTEGER DEFAULT 0,
    resolution_score INTEGER DEFAULT 0,
    communication_score INTEGER DEFAULT 0,
    empathy_score INTEGER DEFAULT 0,
    compliance_score INTEGER DEFAULT 0,
    comments TEXT,
    strengths TEXT,
    improvements TEXT,
    status VARCHAR(20) DEFAULT 'DRAFT',
    evaluation_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);
