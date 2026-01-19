-- V5__create_project_members.sql
-- Project <-> Employee many-to-many relationship

SET search_path TO erm, public;

CREATE TABLE project_members (
    project_id UUID NOT NULL,
    employee_id UUID NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT now(),

    PRIMARY KEY (project_id, employee_id),

    CONSTRAINT fk_project_members_project
        FOREIGN KEY (project_id)
        REFERENCES projects(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_project_members_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_pm_project_id
    ON project_members (project_id);

CREATE INDEX idx_pm_employee_id
    ON project_members (employee_id);
