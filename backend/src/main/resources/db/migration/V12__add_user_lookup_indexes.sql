-- Lookup indexes for common auth and role queries
SET search_path TO erm, public;

CREATE INDEX IF NOT EXISTS idx_users_organization_id
    ON users (organization_id);

CREATE INDEX IF NOT EXISTS idx_user_roles_user_id
    ON user_roles (user_id);

CREATE INDEX IF NOT EXISTS idx_user_roles_organization_id
    ON user_roles (organization_id);
