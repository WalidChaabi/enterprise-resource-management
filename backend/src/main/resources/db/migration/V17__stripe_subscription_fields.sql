-- V17__stripe_subscription_fields.sql
SET search_path TO erm, public;

ALTER TABLE organizations
    ADD COLUMN IF NOT EXISTS stripe_subscription_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS subscription_status VARCHAR(50),
    ADD COLUMN IF NOT EXISTS seat_limit INTEGER;

CREATE TABLE IF NOT EXISTS stripe_webhook_events (
    id VARCHAR(255) PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT now()
);
