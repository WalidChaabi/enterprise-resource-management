-- Search performance indexes for ILIKE/LIKE queries scoped by organization

CREATE INDEX IF NOT EXISTS idx_projects_org_name ON projects (organization_id, name);
CREATE INDEX IF NOT EXISTS idx_tasks_title ON tasks (title);
CREATE INDEX IF NOT EXISTS idx_departments_org_name ON departments (organization_id, name);
CREATE INDEX IF NOT EXISTS idx_time_entries_description ON time_entries (description);
CREATE INDEX IF NOT EXISTS idx_leave_requests_reason ON leave_requests (reason);
