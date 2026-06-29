-- V18__audit_events.sql
SET search_path TO erm, public;

CREATE TABLE IF NOT EXISTS audit_events (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    actor_email VARCHAR(255) NOT NULL,
    actor_id UUID,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID,
    payload_json JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_audit_events_org_created
    ON audit_events (organization_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_audit_events_org_action
    ON audit_events (organization_id, action);
