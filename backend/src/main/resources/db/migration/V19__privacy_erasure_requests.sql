CREATE TABLE erm.privacy_erasure_requests (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES erm.organizations(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES erm.users(id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL,
    requested_at TIMESTAMP NOT NULL DEFAULT NOW(),
    processed_at TIMESTAMP,
    processed_by_id UUID REFERENCES erm.users(id),
    admin_notes TEXT,
    CONSTRAINT chk_privacy_erasure_status CHECK (status IN ('PENDING', 'REJECTED', 'COMPLETED'))
);

CREATE INDEX idx_privacy_erasure_org_status ON erm.privacy_erasure_requests (organization_id, status);
CREATE INDEX idx_privacy_erasure_user_status ON erm.privacy_erasure_requests (user_id, status);
