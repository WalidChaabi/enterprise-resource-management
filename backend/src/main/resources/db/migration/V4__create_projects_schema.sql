-- V4__create_projects_schema.sql
-- Projects core table (no M2M here)

SET search_path TO erm, public;

CREATE TABLE projects (
    id UUID PRIMARY KEY,

    organization_id UUID NOT NULL,
    manager_id UUID,

    name VARCHAR(255) NOT NULL,
    description TEXT,

    status VARCHAR(50) NOT NULL,

    start_date DATE,
    end_date DATE,
    deadline DATE,

    budget NUMERIC(15,2),
    cost NUMERIC(15,2),

    archived BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_projects_organization
        FOREIGN KEY (organization_id)
        REFERENCES organizations(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_projects_manager
        FOREIGN KEY (manager_id)
        REFERENCES users(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_projects_org_id
    ON projects (organization_id);

CREATE INDEX idx_projects_manager_id
    ON projects (manager_id);

CREATE INDEX idx_projects_archived
    ON projects (archived);
