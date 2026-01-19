-- Seed data for testing
-- Password: "password123" (BCrypt hash)

INSERT INTO erm.organizations (id, name, code, is_active, created_at, updated_at)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'Demo Organization', 'DEMO', true, NOW(), NOW())
ON CONFLICT DO NOTHING;

INSERT INTO erm.roles (id, organization_id, code, name, description, scope, created_at, updated_at)
VALUES 
    ('660e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440000', 'ADMIN', 'Administrator', 'Full system access', 'ORG', NOW(), NOW()),
    ('660e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440000', 'USER', 'User', 'Standard user', 'ORG', NOW(), NOW()),
    ('660e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440000', 'HR_ADMIN', 'HR Administrator', 'HR management access', 'ORG', NOW(), NOW()),
    ('660e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440000', 'ORG_ADMIN', 'Organization Administrator', 'Organization management access', 'ORG', NOW(), NOW()),
    ('660e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440000', 'DEPARTMENT_MANAGER', 'Department Manager', 'Department management access', 'ORG', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Create default departments
INSERT INTO erm.departments (id, organization_id, name, code, is_active, created_at, updated_at)
VALUES 
    ('880e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440000', 'Human Resources', 'HR', true, NOW(), NOW()),
    ('880e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440000', 'Engineering', 'ENG', true, NOW(), NOW()),
    ('880e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440000', 'Sales', 'SALES', true, NOW(), NOW()),
    ('880e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440000', 'Marketing', 'MKT', true, NOW(), NOW()),
    ('880e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440000', 'Finance', 'FIN', true, NOW(), NOW())
ON CONFLICT DO NOTHING;

INSERT INTO erm.users (id, organization_id, email, password_hash, first_name, last_name, is_active, created_at, updated_at)
VALUES 
    ('770e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440000', 'admin@demo.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', 'User', true, NOW(), NOW())
ON CONFLICT DO NOTHING;

INSERT INTO erm.employees ( id, organization_id, user_id, employee_code, job_title, is_active, created_at, updated_at)
VALUES (
    '990e8400-e29b-41d4-a716-446655440001',
    '550e8400-e29b-41d4-a716-446655440000',
    '770e8400-e29b-41d4-a716-446655440003',
    'EMP-001',
    'Administrator',
    true,
    NOW(),
    NOW()
)
ON CONFLICT DO NOTHING;

INSERT INTO erm.user_roles (user_id, role_id, organization_id, assigned_at)
VALUES 
    ('770e8400-e29b-41d4-a716-446655440003', '660e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440000', NOW()),
    ('770e8400-e29b-41d4-a716-446655440003', '660e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440000', NOW()),
    ('770e8400-e29b-41d4-a716-446655440003', '660e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440000', NOW())
ON CONFLICT DO NOTHING;

