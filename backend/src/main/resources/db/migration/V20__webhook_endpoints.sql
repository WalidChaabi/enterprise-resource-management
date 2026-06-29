CREATE TABLE erm.webhook_endpoints (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES erm.organizations(id) ON DELETE CASCADE,
    url VARCHAR(2048) NOT NULL,
    secret VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    event_types JSONB NOT NULL DEFAULT '[]'::jsonb,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_webhook_endpoints_org_active ON erm.webhook_endpoints (organization_id, active);

CREATE TABLE erm.webhook_deliveries (
    id UUID PRIMARY KEY,
    endpoint_id UUID NOT NULL REFERENCES erm.webhook_endpoints(id) ON DELETE CASCADE,
    organization_id UUID NOT NULL REFERENCES erm.organizations(id) ON DELETE CASCADE,
    event_type VARCHAR(100) NOT NULL,
    payload_json JSONB NOT NULL,
    status VARCHAR(32) NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    max_attempts INT NOT NULL DEFAULT 5,
    next_retry_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_attempt_at TIMESTAMP,
    response_status INT,
    response_body TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    delivered_at TIMESTAMP,
    CONSTRAINT chk_webhook_delivery_status CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'DEAD'))
);

CREATE INDEX idx_webhook_deliveries_retry ON erm.webhook_deliveries (status, next_retry_at);
CREATE INDEX idx_webhook_deliveries_org_created ON erm.webhook_deliveries (organization_id, created_at DESC);
