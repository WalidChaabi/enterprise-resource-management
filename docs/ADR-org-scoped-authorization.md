# ADR: Organization-scoped authorization

## Status

Accepted (Portfolio v1.0 — Phase 1, Task 1.1)

## Context

The ERM platform is multi-tenant: core data is scoped by `organization_id`. Authorization was implemented through three inconsistent mechanisms:

1. Spring `@PreAuthorize("hasRole('ORG_ADMIN')")` using **global** authorities from `CustomUserDetailsService` (roles from all organizations).
2. Imperative `PermissionChecker` calls in some services/controllers (membership incorrectly tied to `users.organization_id`).
3. Repository query filters by `organizationId` (data scoping, not access control).

This allowed users with a role in organization A to pass authorization checks for organization B when only the path UUID changed.

`UserRole` is the source of truth for per-organization membership and roles. `OrganizationSwitcherService` already validates access via `user_roles`; authorization must use the same model.

## Decision

### Canonical service

Introduce **`OrganizationSecurityService`** registered as Spring bean `organizationSecurity` for SpEL:

| Method | Purpose |
|--------|---------|
| `hasOrgAccess(orgId)` / `hasOrgMembership(orgId)` | User has at least one `user_roles` row for org |
| `hasAnyOrgRole(orgId, 'ROLE', ...)` | Membership + role in that org |
| `requireOrgMembership(orgId)` | Throws `AccessDeniedException` if not member |
| `requireAnyOrgRole(orgId, 'ROLE', ...)` | Throws if not member or missing role |

Overloadings with explicit `userEmail` support service-layer checks during migration.

**Membership rule:** `userRoleRepository.findByUserIdAndOrganizationId(userId, organizationId)` is non-empty.

**Role rule:** Among roles in that org, at least one `roles.code` matches a required code.

### Controller authorization (target pattern — Task 1.2)

```java
@PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'ORG_ADMIN', 'HR_ADMIN')")
```

Do **not** use global `hasRole('ORG_ADMIN')` on paths containing `{organizationId}`.

### Defense in depth

| Layer | Responsibility |
|-------|----------------|
| JWT filter | Identity only (Task 1.2: remove global role authorities) |
| Org context filter | JWT `organizationId` claim vs path org (Task 1.3) |
| `@PreAuthorize` | Org membership + role via `@organizationSecurity` |
| Service layer | `organizationSecurity.require*` for endpoints without method security or complex rules |
| Repository | Always filter by `organizationId` in queries |

### Deprecation

`PermissionChecker` is a **deprecated facade** that delegates to `OrganizationSecurityService`. New code must inject `OrganizationSecurityService` directly.

### JWT active organization (Task 1.3)

`OrganizationContextFilter` rejects requests where the path `{organizationId}` does not match the JWT `organizationId` claim. Users must call `POST /api/organizations/switch` to change active organization.

## Consequences

### Positive

- Single documented authorization model aligned with schema and org switcher.
- SpEL expressions are explicit about organization context.
- Testable bean independent of Spring Security global authorities.

### Negative / follow-up

- **Task 1.2:** Refactor `CustomUserDetailsService` to stop loading global roles; migrate controller `@PreAuthorize`.
- **Task 1.3:** Enforce JWT active-org claim against path `{organizationId}`.
- **Task 1.4:** Apply `OrganizationSecurityService` to `SearchService`, reporting services, and remaining gaps.
- **Task 2.2:** Cross-tenant integration tests.

## References

- [authorization-coverage-matrix.md](./authorization-coverage-matrix.md)
- [ADR-auth-session.md](./ADR-auth-session.md)
- [SECURITY_TEST_PLAN.md](./SECURITY_TEST_PLAN.md)
- `OrganizationSecurityService.java`
