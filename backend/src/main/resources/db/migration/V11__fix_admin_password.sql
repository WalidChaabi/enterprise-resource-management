-- Fix admin demo account password hash (was incorrect in V3 seed data)
-- Password: password123
UPDATE erm.users
SET password_hash = '$2a$10$2b1Dl087qxHWfF2SQdqWQuIiXUgbosVemh3eKXEQEeGDF1sOQ6w56',
    updated_at = NOW()
WHERE email = 'admin@demo.com';
