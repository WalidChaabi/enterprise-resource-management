-- V16__organization_subscription_fields.sql
-- Subscription stubs for MT-B (Stripe); nullable until billing is wired.
SET search_path TO erm, public;

ALTER TABLE organizations
    ADD COLUMN IF NOT EXISTS plan_code VARCHAR(50),
    ADD COLUMN IF NOT EXISTS stripe_customer_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS trial_ends_at TIMESTAMP;
