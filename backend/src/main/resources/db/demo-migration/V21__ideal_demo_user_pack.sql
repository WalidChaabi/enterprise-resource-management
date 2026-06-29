-- Ideal demo pack: five persona logins + sample workflow data for local QA.
-- Runs after V3 on fresh installs; idempotent on databases that already applied V3.
-- Password for all accounts: password123 (BCrypt hash matches V11 fix)

SET search_path TO erm, public;

-- ---------------------------------------------------------------------------
-- Roles (fix legacy V3 codes before upserting canonical codes — avoids uq_roles_code_per_org clash)
-- ---------------------------------------------------------------------------
UPDATE erm.roles SET code = 'PROJECT_MANAGER', name = 'Project Manager', description = 'Project and team management', updated_at = NOW()
WHERE id = '660e8400-e29b-41d4-a716-446655440004' AND organization_id = '550e8400-e29b-41d4-a716-446655440000' AND code = 'ORG_ADMIN';

UPDATE erm.roles SET code = 'ORG_ADMIN', name = 'Organization Administrator', description = 'Full organization access', updated_at = NOW()
WHERE id = '660e8400-e29b-41d4-a716-446655440001' AND organization_id = '550e8400-e29b-41d4-a716-446655440000' AND code = 'ADMIN';

UPDATE erm.roles SET code = 'EMPLOYEE', name = 'Employee', description = 'Standard employee access', updated_at = NOW()
WHERE id = '660e8400-e29b-41d4-a716-446655440002' AND organization_id = '550e8400-e29b-41d4-a716-446655440000' AND code = 'USER';

INSERT INTO erm.roles (id, organization_id, code, name, description, scope, created_at, updated_at)
VALUES
    ('660e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440000', 'ORG_ADMIN', 'Organization Administrator', 'Full organization access', 'ORG', NOW(), NOW()),
    ('660e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440000', 'EMPLOYEE', 'Employee', 'Standard employee access', 'ORG', NOW(), NOW()),
    ('660e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440000', 'HR_ADMIN', 'HR Administrator', 'HR management access', 'ORG', NOW(), NOW()),
    ('660e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440000', 'PROJECT_MANAGER', 'Project Manager', 'Project and team management', 'ORG', NOW(), NOW()),
    ('660e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440000', 'DEPARTMENT_MANAGER', 'Department Manager', 'Department management access', 'ORG', NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    code = EXCLUDED.code,
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    updated_at = NOW();

-- ---------------------------------------------------------------------------
-- Users & employees (five personas)
-- ---------------------------------------------------------------------------
UPDATE erm.users
SET first_name = 'Alex', last_name = 'Admin', updated_at = NOW()
WHERE id = '770e8400-e29b-41d4-a716-446655440003' AND email = 'admin@demo.com';

INSERT INTO erm.users (id, organization_id, department_id, email, password_hash, first_name, last_name, is_active, created_at, updated_at)
VALUES
    ('770e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440000', '880e8400-e29b-41d4-a716-446655440001', 'hr@demo.com', '$2a$10$2b1Dl087qxHWfF2SQdqWQuIiXUgbosVemh3eKXEQEeGDF1sOQ6w56', 'Helen', 'Reed', true, NOW(), NOW()),
    ('770e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440000', '880e8400-e29b-41d4-a716-446655440002', 'pm@demo.com', '$2a$10$2b1Dl087qxHWfF2SQdqWQuIiXUgbosVemh3eKXEQEeGDF1sOQ6w56', 'Paul', 'Martin', true, NOW(), NOW()),
    ('770e8400-e29b-41d4-a716-446655440006', '550e8400-e29b-41d4-a716-446655440000', '880e8400-e29b-41d4-a716-446655440002', 'employee@demo.com', '$2a$10$2b1Dl087qxHWfF2SQdqWQuIiXUgbosVemh3eKXEQEeGDF1sOQ6w56', 'Emma', 'Employee', true, NOW(), NOW()),
    ('770e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440000', '880e8400-e29b-41d4-a716-446655440002', 'manager@demo.com', '$2a$10$2b1Dl087qxHWfF2SQdqWQuIiXUgbosVemh3eKXEQEeGDF1sOQ6w56', 'Mike', 'Manager', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO erm.employees (id, organization_id, user_id, employee_code, job_title, is_active, created_at, updated_at)
VALUES
    ('990e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440000', '770e8400-e29b-41d4-a716-446655440004', 'EMP-002', 'HR Business Partner', true, NOW(), NOW()),
    ('990e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440000', '770e8400-e29b-41d4-a716-446655440005', 'EMP-003', 'Project Manager', true, NOW(), NOW()),
    ('990e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440000', '770e8400-e29b-41d4-a716-446655440006', 'EMP-004', 'Software Engineer', true, NOW(), NOW()),
    ('990e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440000', '770e8400-e29b-41d4-a716-446655440007', 'EMP-005', 'Engineering Manager', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

UPDATE erm.employees
SET employee_code = 'EMP-001', job_title = 'Organization Administrator', updated_at = NOW()
WHERE id = '990e8400-e29b-41d4-a716-446655440001';

-- Admin: single ORG_ADMIN role only
DELETE FROM erm.user_roles
WHERE user_id = '770e8400-e29b-41d4-a716-446655440003'
  AND organization_id = '550e8400-e29b-41d4-a716-446655440000'
  AND role_id <> '660e8400-e29b-41d4-a716-446655440001';

INSERT INTO erm.user_roles (user_id, role_id, organization_id, assigned_at)
VALUES
    ('770e8400-e29b-41d4-a716-446655440003', '660e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440000', NOW()),
    ('770e8400-e29b-41d4-a716-446655440004', '660e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440000', NOW()),
    ('770e8400-e29b-41d4-a716-446655440005', '660e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440000', NOW()),
    ('770e8400-e29b-41d4-a716-446655440006', '660e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440000', NOW()),
    ('770e8400-e29b-41d4-a716-446655440007', '660e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440000', NOW())
ON CONFLICT DO NOTHING;

-- Pro plan unlocks webhooks, billing demos, etc.
UPDATE erm.organizations
SET plan_code = 'PRO',
    trial_ends_at = NOW() + INTERVAL '365 days',
    seat_limit = NULL,
    updated_at = NOW()
WHERE id = '550e8400-e29b-41d4-a716-446655440000';

-- ---------------------------------------------------------------------------
-- Sample projects, tasks, time entries, and leave requests
-- ---------------------------------------------------------------------------
INSERT INTO erm.projects (id, organization_id, manager_id, name, description, status, start_date, end_date, deadline, budget, cost, archived, created_at, updated_at)
VALUES
    ('aa0e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440000', '770e8400-e29b-41d4-a716-446655440005',
     'Customer Portal Redesign', 'Modernize the customer-facing portal with SSO and self-service billing.', 'ACTIVE',
     CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE + INTERVAL '60 days', CURRENT_DATE + INTERVAL '45 days', 120000.00, 42000.00, false, NOW(), NOW()),
    ('aa0e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440000', '770e8400-e29b-41d4-a716-446655440005',
     'HR Analytics Dashboard', 'Leave balances, headcount trends, and department capacity views.', 'PLANNING',
     CURRENT_DATE + INTERVAL '7 days', CURRENT_DATE + INTERVAL '90 days', CURRENT_DATE + INTERVAL '30 days', 45000.00, NULL, false, NOW(), NOW()),
    ('aa0e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440000', '770e8400-e29b-41d4-a716-446655440005',
     'Legacy System Migration', 'Decommission on-prem ERP integrations.', 'ON_HOLD',
     CURRENT_DATE - INTERVAL '90 days', NULL, CURRENT_DATE + INTERVAL '120 days', 80000.00, 15000.00, false, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO erm.project_members (project_id, employee_id, created_at)
VALUES
    ('aa0e8400-e29b-41d4-a716-446655440001', '990e8400-e29b-41d4-a716-446655440003', NOW()),
    ('aa0e8400-e29b-41d4-a716-446655440001', '990e8400-e29b-41d4-a716-446655440004', NOW()),
    ('aa0e8400-e29b-41d4-a716-446655440001', '990e8400-e29b-41d4-a716-446655440005', NOW()),
    ('aa0e8400-e29b-41d4-a716-446655440002', '990e8400-e29b-41d4-a716-446655440002', NOW()),
    ('aa0e8400-e29b-41d4-a716-446655440002', '990e8400-e29b-41d4-a716-446655440003', NOW())
ON CONFLICT DO NOTHING;

INSERT INTO erm.tasks (id, project_id, assigned_to_id, title, description, status, priority, due_date, created_at, updated_at)
VALUES
    ('bb0e8400-e29b-41d4-a716-446655440001', 'aa0e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440006',
     'Implement authentication API', 'OAuth2 login flow and session refresh.', 'IN_PROGRESS', 'HIGH', CURRENT_DATE + INTERVAL '5 days', NOW(), NOW()),
    ('bb0e8400-e29b-41d4-a716-446655440002', 'aa0e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440006',
     'Build billing settings UI', 'Stripe portal link and plan display.', 'TODO', 'MEDIUM', CURRENT_DATE + INTERVAL '12 days', NOW(), NOW()),
    ('bb0e8400-e29b-41d4-a716-446655440003', 'aa0e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440004',
     'Define leave metrics schema', 'Align HR reporting fields with capacity dashboard.', 'REVIEW', 'MEDIUM', CURRENT_DATE + INTERVAL '10 days', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO erm.time_entries (id, employee_id, project_id, task_id, entry_date, start_time, end_time, duration_hours, description, status, approved_by_id, approved_at, created_at, updated_at)
VALUES
    ('cc0e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440006', 'aa0e8400-e29b-41d4-a716-446655440001', 'bb0e8400-e29b-41d4-a716-446655440001',
     CURRENT_DATE, '09:00', '12:30', 3.50, 'Auth endpoint scaffolding', 'DRAFT', NULL, NULL, NOW(), NOW()),
    ('cc0e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440006', 'aa0e8400-e29b-41d4-a716-446655440001', 'bb0e8400-e29b-41d4-a716-446655440001',
     CURRENT_DATE - INTERVAL '1 day', '09:00', '17:00', 8.00, 'JWT integration and tests', 'SUBMITTED', NULL, NULL, NOW(), NOW()),
    ('cc0e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440006', 'aa0e8400-e29b-41d4-a716-446655440001', 'bb0e8400-e29b-41d4-a716-446655440002',
     CURRENT_DATE - INTERVAL '3 days', '10:00', '15:00', 5.00, 'Component library setup', 'APPROVED', '770e8400-e29b-41d4-a716-446655440005', NOW() - INTERVAL '2 days', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO erm.leave_requests (id, employee_id, leave_type, start_date, end_date, total_days, reason, status, approved_by_id, approved_at, created_at, updated_at)
VALUES
    ('dd0e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440006', 'VACATION',
     CURRENT_DATE + INTERVAL '14 days', CURRENT_DATE + INTERVAL '18 days', 5,
     'Family trip — pending manager approval', 'PENDING', NULL, NULL, NOW(), NOW()),
    ('dd0e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440006', 'SICK_LEAVE',
     CURRENT_DATE - INTERVAL '10 days', CURRENT_DATE - INTERVAL '9 days', 2,
     'Flu recovery', 'APPROVED', '770e8400-e29b-41d4-a716-446655440007', NOW() - INTERVAL '12 days', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO erm.notifications (id, user_id, title, description, time, unread)
VALUES
    ('ee0e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440005',
     'Time entry awaiting approval', 'Emma Employee submitted 8.0 hours on Customer Portal Redesign.', NOW() - INTERVAL '2 hours', true),
    ('ee0e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440007',
     'Leave request pending', 'Emma Employee requested 5 days of vacation starting in two weeks.', NOW() - INTERVAL '1 hour', true),
    ('ee0e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440006',
     'Task assigned', 'You were assigned "Implement authentication API".', NOW() - INTERVAL '1 day', false)
ON CONFLICT (id) DO NOTHING;
