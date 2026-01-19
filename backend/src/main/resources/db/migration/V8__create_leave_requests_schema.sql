-- V8__create_leave_requests_schema.sql
-- Leave requests table for leave management

SET search_path TO erm, public;

CREATE TABLE leave_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    employee_id UUID NOT NULL,

    leave_type VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_days INTEGER NOT NULL,

    reason TEXT,

    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    approved_by_id UUID,
    approved_at TIMESTAMP,
    rejection_reason TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_leave_requests_employee
        FOREIGN KEY (employee_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_leave_requests_approved_by
        FOREIGN KEY (approved_by_id)
        REFERENCES users(id)
        ON DELETE SET NULL,

    CONSTRAINT chk_leave_requests_dates
        CHECK (end_date >= start_date),

    CONSTRAINT chk_leave_requests_total_days_positive
        CHECK (total_days > 0),

    CONSTRAINT chk_leave_requests_start_date_not_past
        CHECK (start_date >= CURRENT_DATE - INTERVAL '30 days')
);

CREATE INDEX idx_leave_requests_employee_id
    ON leave_requests (employee_id);

CREATE INDEX idx_leave_requests_status
    ON leave_requests (status);

CREATE INDEX idx_leave_requests_leave_type
    ON leave_requests (leave_type);

CREATE INDEX idx_leave_requests_start_date
    ON leave_requests (start_date);

CREATE INDEX idx_leave_requests_end_date
    ON leave_requests (end_date);

CREATE INDEX idx_leave_requests_employee_status
    ON leave_requests (employee_id, status);

CREATE INDEX idx_leave_requests_date_range
    ON leave_requests (start_date, end_date);

