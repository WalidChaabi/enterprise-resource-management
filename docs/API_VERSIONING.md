# API versioning policy

## URL structure

| Prefix | Scope | Versioning |
|--------|--------|------------|
| `/api/v1/organizations/{organizationId}/**` | Org-scoped resources (employees, projects, reports, …) | **Versioned** (`v1`) |
| `/api/settings/**` | Current-user settings (profile, billing, security) | **Unversioned** — tied to session/JWT active org |
| `/api/organizations/**` | Org switcher (list, switch) | **Unversioned** |
| `/api/notifications/**` | User notifications | **Unversioned** |
| `/auth/**` | Login, refresh, MFA, signup | **Unversioned** |
| `/webhooks/stripe` | Stripe callbacks | **Unversioned** |

## Path variable naming

All org-scoped controllers use **`{organizationId}`** (UUID) in the path template. Do not use `{orgId}` in new endpoints.

SpEL examples:

```java
@PreAuthorize("@organizationSecurity.hasAnyOrgRole(#organizationId, 'ORG_ADMIN')")
```

## Breaking changes

1. Introduce `/api/v2/...` for incompatible contract changes.
2. Keep `v1` available for at least one release cycle after `v2` ships.
3. Document deprecations in OpenAPI (`@Deprecated` + release notes).

## OpenAPI

- Swagger UI: `/swagger-ui.html` (disabled in `prod` profile).
- Org resources are grouped under versioned paths; settings/auth are documented with a note that they are session-scoped and unversioned.

## Related

- [ADR-org-scoped-authorization.md](./ADR-org-scoped-authorization.md)
- [authorization-coverage-matrix.md](./authorization-coverage-matrix.md)
