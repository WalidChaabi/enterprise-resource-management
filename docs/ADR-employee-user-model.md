# ADR: Employee vs User identity model

## Status

Accepted (Portfolio v1.0 — Phase 3, Task 3.7)

## Context

The platform has two related concepts:

1. **`User`** — authentication identity (email, password/SSO, org membership, department, roles).
2. **`Employee`** — HR profile row (`employee_code`, `job_title`) linked to a `User` via `employees.user_id`.

Operational data uses both patterns inconsistently:

| Domain | FK target | Notes |
|--------|-----------|-------|
| `time_entries.employee_id` | `users.id` | JPA field named `employee`, type `User` |
| `leave_requests.employee_id` | `users.id` | Same |
| `project_members.employee_id` | `employees.id` | Project staffing uses HR profile |
| Employee list API | `users` table | Paginated via `UserRepository` |
| Capacity / allocation reports | `Employee` + `User` | Bridge via `EmployeeRepository.findByUserId` |

Removing `Employee` would require a large schema migration (`project_members`, reporting, HR APIs) with high regression risk. Merging tables would also conflate auth concerns with HR metadata.

## Decision

**Keep both entities with explicit roles:**

| Entity | Source of truth for |
|--------|---------------------|
| `User` | Login, authorization, org/department, person name/email, time & leave ownership |
| `Employee` | HR-specific attributes and project membership (`project_members`) |

**Linking rule:** Every person who logs time or requests leave must have a `User`. An `Employee` row is created for HR/project staffing and **must** reference that user (`employees.user_id`). `Employee.id` is a separate UUID (not equal to `user.id`).

**Service conventions:**

- Time/leave services use `User` as the workforce actor (`employee_id` → `users.id`).
- Project membership and employee directory use `Employee`.
- When resolving display names on projects, load `employee.user` (or `findByUserId`) — never assume `employee.id == user.id`.
- New features: prefer `User` for workflow actors; use `Employee` only when HR fields or `project_members` are involved.

## Consequences

- No Flyway merge migration in v1; dual table remains.
- Reporting/capacity code continues to bridge via `EmployeeRepository.findByUserId`.
- Future consolidation (single `workforce_profile` view) is optional and deferred.
- API docs should describe employee list endpoints as org users with optional HR profile enrichment.

## References

- `backend/src/main/java/com/company/erm/entity/Employee.java`
- `backend/src/main/java/com/company/erm/entity/User.java`
- `backend/src/main/java/com/company/erm/service/EmployeeService.java`
