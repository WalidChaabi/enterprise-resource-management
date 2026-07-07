-- MT-D: SSO/OIDC + MFA (TOTP)

ALTER TABLE erm.organizations
    ADD COLUMN IF NOT EXISTS sso_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS require_mfa BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS sso_provider VARCHAR(20);

ALTER TABLE erm.users
    ALTER COLUMN password_hash DROP NOT NULL;

CREATE TABLE IF NOT EXISTS erm.user_identity_links (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES erm.users(id) ON DELETE CASCADE,
    provider VARCHAR(50) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_identity_provider_subject UNIQUE (provider, subject)
);

CREATE INDEX IF NOT EXISTS idx_user_identity_links_user_id ON erm.user_identity_links(user_id);

CREATE TABLE IF NOT EXISTS erm.user_mfa_secrets (
    user_id UUID PRIMARY KEY REFERENCES erm.users(id) ON DELETE CASCADE,
    secret_encrypted VARCHAR(512) NOT NULL,
    enabled_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS erm.user_mfa_backup_codes (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES erm.users(id) ON DELETE CASCADE,
    code_hash VARCHAR(255) NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_user_mfa_backup_codes_user_id ON erm.user_mfa_backup_codes(user_id);
