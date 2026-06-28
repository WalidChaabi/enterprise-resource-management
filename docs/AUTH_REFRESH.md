# Authentication refresh flow

## Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/auth/login` | Public | Issue access + refresh cookies |
| POST | `/auth/refresh` | Refresh cookie | Rotate refresh token, new access cookie |
| POST | `/auth/logout` | Optional cookies | Revoke refresh token, clear cookies |

## Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `jwt.expiration` | `900000` (15 min) | Access token TTL (ms) |
| `jwt.refresh-expiration` | `604800000` (7 days) | Refresh token TTL (ms) |
| `auth.cookie.secure` | `false` (dev) / `true` (prod) | `Secure` flag |
| `auth.cookie.same-site` | `Lax` | SameSite policy |

## Frontend usage

```typescript
// Login
this.authService.login({ email, password }).subscribe();

// All API calls — auth.interceptor sets withCredentials: true

// 401 handling — error.interceptor calls refreshSession() once
```

## Token rotation

Each successful `/auth/refresh`:

1. Validates refresh cookie against `refresh_tokens` table (hash, not revoked, not expired).
2. Revokes the used token and links it to the replacement row.
3. Issues new access + refresh cookies.

## Rate limiting

`POST /auth/login` is limited to **10 requests/minute per client IP** (via `X-Forwarded-For` when behind nginx).
