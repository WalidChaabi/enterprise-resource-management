# Security test plan (manual matrix)

Use after Phase 4 hardening. Run against a dev/staging environment with at least two organizations.

## Authentication

| # | Test | Expected |
|---|------|----------|
| A1 | Login with valid credentials | 200, httpOnly cookies set, no token in browser `localStorage` |
| A2 | Login with wrong password 11× in 1 min | 429 on `/auth/login` |
| A3 | Call protected API without cookies | 401 |
| A4 | Wait for access token expiry, call API | Auto-refresh via `/auth/refresh` or 401 → login |
| A5 | Logout | Cookies cleared, refresh token revoked in DB |
| A6 | Reuse old refresh token after rotation | 401 |

## Authorization (Quick Actions)

| # | Test | Role | Endpoint | Expected |
|---|------|------|----------|----------|
| Q1 | Create employee | EMPLOYEE | `POST /api/employees` | 403 |
| Q2 | Create employee | HR_ADMIN | `POST /api/employees` | 201 |
| Q3 | Create project | EMPLOYEE | `POST /api/projects` | 403 |
| Q4 | Create project | PROJECT_MANAGER | `POST /api/projects` | 201 |

## Multi-tenant isolation

| # | Test | Expected |
|---|------|----------|
| T1 | User A requests org B project by ID | 404 or 403 |
| T2 | User A lists org B employees | Empty or 403 |
| T3 | Org switch without membership | 403 |

## Production surface

| # | Test | Expected |
|---|------|----------|
| P1 | `GET /swagger-ui/index.html` with `prod` profile | 404 or 401 |
| P2 | `GET /v3/api-docs` with `prod` profile | Disabled |
| P3 | CORS preflight from unknown origin | Rejected |

## Notes

- Automated integration tests cover auth login, role guards, and project isolation.
- Pen-test items T1–T3 should be verified with two seeded organizations.
