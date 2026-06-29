-- V15__password_reset_tokens.sql
SET search_path TO erm, public;

CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_password_reset_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE UNIQUE INDEX idx_password_reset_token_hash
    ON password_reset_tokens (token_hash)
    WHERE used_at IS NULL;

CREATE INDEX idx_password_reset_user_id
    ON password_reset_tokens (user_id);
