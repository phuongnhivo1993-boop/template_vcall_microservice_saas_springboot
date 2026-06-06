ALTER TABLE users
    ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS email_verification_token VARCHAR(512),
    ADD COLUMN IF NOT EXISTS email_verification_token_expiry TIMESTAMP;
