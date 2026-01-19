-- V6__create_tasks_schema.sql
-- Tasks table for project task management

SET search_path TO erm, public;

CREATE TABLE tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    project_id UUID NOT NULL,
    assigned_to_id UUID,

    title VARCHAR(255) NOT NULL,
    description TEXT,

    status VARCHAR(50) NOT NULL DEFAULT 'TODO',
    priority VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',

    due_date DATE,
    completed_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_tasks_project
        FOREIGN KEY (project_id)
        REFERENCES projects(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_tasks_assigned_to
        FOREIGN KEY (assigned_to_id)
        REFERENCES users(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_tasks_project_id
    ON tasks (project_id);

CREATE INDEX idx_tasks_assigned_to_id
    ON tasks (assigned_to_id);

CREATE INDEX idx_tasks_status
    ON tasks (status);

CREATE INDEX idx_tasks_priority
    ON tasks (priority);

CREATE INDEX idx_tasks_due_date
    ON tasks (due_date);

