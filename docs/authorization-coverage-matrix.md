# Authorization coverage matrix

Maps org-scoped API surfaces to required checks.  
**Canonical API:** `OrganizationSecurityService` (`@organizationSecurity`).  
**Status key:** `✅` = migrated; `🟡` = delegates via deprecated `PermissionChecker`.

## Org-scoped controllers (`/api/v1/organizations/{organizationId}/...`)

| Controller | Path variable | Class-level `@PreAuthorize` | Target expression | Status |
|------------|---------------|----------------------------|-------------------|--------|
| `EmployeeController` | `organizationId` | Org-scoped SpEL | `@organizationSecurity.hasAnyOrgRole(...)` | ✅ Task 1.2 |
| `DepartmentController` | `organizationId` | Org-scoped SpEL | Same pattern | ✅ Task 1.2 |
| `ProjectController` | `organizationId` | Org-scoped SpEL | `@organizationSecurity.hasAnyOrgRole(...)` | ✅ Task 3.1 |
| `TaskController` | `organizationId` | Org-scoped SpEL | Org-scoped SpEL | ✅ Task 1.2 |
| `TimeEntryController` | `organizationId` | Org-scoped SpEL | Org-scoped SpEL | ✅ Task 1.2 |
| `LeaveRequestController` | `organizationId` | Org-scoped SpEL | Org-scoped SpEL | ✅ Task 1.2 |
| `RoleController` | `organizationId` | Org-scoped SpEL | Org-scoped SpEL | ✅ Task 1.2 |
| `UserRoleController` | `organizationId` | Org-scoped SpEL | Org-scoped SpEL | ✅ Task 1.2 |
| `SearchController` | `organizationId` | `hasOrgAccess` | Membership + service-layer check | ✅ Task 1.4 |
| `ReportingController` | `organizationId` | Org-scoped SpEL + dashboard membership | All endpoints org-scoped | ✅ Task 1.2 |
| `AuditEventController` | `organizationId` | Org-scoped SpEL | `ORG_ADMIN` | ✅ Task 1.2 |
| `WebhookController` | `organizationId` | Org-scoped SpEL | `ORG_ADMIN` | ✅ Task 1.2 |
| `OrganizationPrivacyController` | `organizationId` | Org-scoped SpEL | Org-scoped SpEL | ✅ Task 1.2 |

## User-scoped controllers (no path org)

| Controller | Scope | Authorization model | Status |
|------------|-------|---------------------|--------|
| `SettingsController` | JWT active org | `@organizationSecurity.hasAnyOrgRoleInJwtOrganization(...)` for admin endpoints | ✅ Task 1.4 |
| `NotificationController` | Current user | `isAuthenticated()` | ✅ |
| `UserPrivacyController` | `/users/me` | `isAuthenticated()` | ✅ |
| `OrganizationSwitcherController` | `/api/organizations` | `validateOrganizationAccess` via `user_roles` | ✅ |
| `QuickActionsController` | JWT active org | `@organizationSecurity.hasAnyOrgRoleInJwtOrganization(...)` | ✅ Task 1.4 |
| `AuthController` | Public / authenticated | Auth flows | ✅ |
| `UiConfigController` | Authenticated | Low risk | ✅ |
| `StripeWebhookController` | Public + signature | Stripe verification | ✅ |

## Service-layer authorization (Task 1.4)

`PermissionChecker` delegates all role/membership checks to `OrganizationSecurityService`.  
Services should migrate to inject `OrganizationSecurityService` directly.

| Service | Authorization | Status |
|---------|----------------|--------|
| `EmployeeService` | `PermissionChecker` → delegates | 🟡 |
| `DepartmentService` | `PermissionChecker` → delegates | 🟡 |
| `LeaveRequestService` | `PermissionChecker` → delegates | 🟡 |
| `TimeEntryService` | `PermissionChecker` → delegates | 🟡 |
| `TaskService` | `PermissionChecker` → delegates | 🟡 |
| `RoleService` | `PermissionChecker` → delegates | 🟡 |
| `UserRoleService` | `PermissionChecker` → delegates | 🟡 |
| `SettingsService` | `PermissionChecker` → delegates | 🟡 |
| `SecuritySettingsService` | `PermissionChecker` → delegates | 🟡 |
| `AuditEventService` | `PermissionChecker` → delegates | 🟡 |
| `UserPrivacyService` | `PermissionChecker` → delegates | 🟡 |
| `WebhookEndpointService` | `PermissionChecker` → delegates | 🟡 |
| `SearchService` | `requireOrgMembership` | ✅ Task 1.4 |
| `TimeReportService` | `requireOrgMembership` | ✅ Task 1.4 |
| `LeaveReportService` | `requireOrgMembership` | ✅ Task 1.4 |
| `ResourceAllocationService` | `requireOrgMembership` | ✅ Task 1.4 |
| `CapacityDashboardService` | `requireOrgMembership` | ✅ Task 1.4 |
| `DashboardService` | `requireOrgMembership` | ✅ Task 1.4 |

## JWT organization context (Task 1.3)

`OrganizationContextFilter` rejects `/api/v1/organizations/{uuid}/**` when JWT `organizationId` is missing or does not match the path segment.

## SpEL reference

```java
// Membership only
@PreAuthorize("@organizationSecurity.hasOrgAccess(#organizationId)")

// Single role
@PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'ORG_ADMIN')")

// Multiple roles
@PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'ORG_ADMIN', 'HR_ADMIN')")

// JWT active org (no path org id)
@PreAuthorize("@organizationSecurity.hasAnyOrgRoleInJwtOrganization('ORG_ADMIN')")
```

For `ProjectController`, use `#organizationId` like all other org-scoped controllers.

## Related documents

- [ADR-org-scoped-authorization.md](./ADR-org-scoped-authorization.md)
- [PORTFOLIO_IMPLEMENTATION_ROADMAP.md](./PORTFOLIO_IMPLEMENTATION_ROADMAP.md) — Phase 1
