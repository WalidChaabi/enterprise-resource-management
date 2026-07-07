# ADR: Authentication session model

## Status

Accepted (Phase 4)

## Context

The SPA previously stored JWT access tokens in `localStorage`, which is vulnerable to XSS exfiltration. Production exposure requires shorter-lived access tokens and refresh rotation.

## Decision

Use **httpOnly, Secure (prod) cookies** for tokens:

| Cookie | Path | TTL | Purpose |
|--------|------|-----|---------|
| `erm_access_token` | `/` | 15 min (configurable) | JWT access token |
| `erm_refresh_token` | `/auth` | 7 days (configurable) | Opaque refresh token (SHA-256 hash in DB) |

### Flow

1. **Login** — `POST /auth/login` validates credentials, sets both cookies, returns user profile JSON (access token still in body for API clients/tests).
2. **API calls** — Browser sends cookies automatically (`withCredentials: true`). `JwtAuthenticationFilter` reads cookie first, then `Authorization: Bearer` header (tests).
3. **Refresh** — `POST /auth/refresh` rotates refresh token in DB, re-issues cookies.
4. **Logout** — `POST /auth/logout` revokes refresh token and clears cookies.

### Frontend

- No JWT in `localStorage` or `sessionStorage`.
- User profile only in `sessionStorage` (roles, name, org id for UI).
- `errorInterceptor` retries once via `/auth/refresh` on 401.

## Consequences

- Requires CORS `allowCredentials: true` and explicit allowed origins (no `*`).
- CSRF risk mitigated via **`SameSite=Strict`** on access cookies in production (`application-prod.properties`), scoped refresh cookie path (`/auth`), stateless API design, and org-scoped JWT validation on versioned organization routes.
- Development uses `SameSite=Lax` for local SPA workflows.
- Spring CSRF is intentionally disabled for the JSON API; the SPA does not use cookie-based CSRF tokens. Cross-site POST risk is reduced by SameSite cookies and CORS allowlists.
- Integration tests continue using `Authorization: Bearer` from login response body.

See also: [AUTH_REFRESH.md](./AUTH_REFRESH.md), [SECURITY_TEST_PLAN.md](./SECURITY_TEST_PLAN.md).
