-- V7__create_time_entries_schema.sql
-- Time entries table for time tracking

SET search_path TO erm, public;

CREATE TABLE time_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    employee_id UUID NOT NULL,
    project_id UUID NOT NULL,
    task_id UUID,

    entry_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    duration_hours NUMERIC(5,2) NOT NULL,

    description TEXT,

    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    approved_by_id UUID,
    approved_at TIMESTAMP,
    rejection_reason TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_time_entries_employee
        FOREIGN KEY (employee_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_time_entries_project
        FOREIGN KEY (project_id)
        REFERENCES projects(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_time_entries_task
        FOREIGN KEY (task_id)
        REFERENCES tasks(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_time_entries_approved_by
        FOREIGN KEY (approved_by_id)
        REFERENCES users(id)
        ON DELETE SET NULL,

    CONSTRAINT chk_time_entries_duration_positive
        CHECK (duration_hours > 0),

    CONSTRAINT chk_time_entries_date_not_future
        CHECK (entry_date <= CURRENT_DATE)
);

CREATE INDEX idx_time_entries_employee_id
    ON time_entries (employee_id);

CREATE INDEX idx_time_entries_project_id
    ON time_entries (project_id);

CREATE INDEX idx_time_entries_task_id
    ON time_entries (task_id);

CREATE INDEX idx_time_entries_entry_date
    ON time_entries (entry_date);

CREATE INDEX idx_time_entries_status
    ON time_entries (status);

CREATE INDEX idx_time_entries_employee_date
    ON time_entries (employee_id, entry_date);

