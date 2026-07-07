# ERM Platform — Operations Runbook

Quick reference for running, monitoring, and troubleshooting the ERM stack in dev and production.

**Last updated:** 2026-06-27

---

## Stack overview

| Service | Port (default) | Health |
|---------|----------------|--------|
| Frontend (nginx) | 4200 | `GET /health` |
| Backend API | 8080 | `GET /actuator/health` |
| PostgreSQL | 5432 (internal) | `pg_isready` |

---

## Start / stop

### Local Docker (production-like)

```bash
cd docker
cp env.example .env   # edit secrets before prod use
docker compose up -d --build
```

### Dev stack

```bash
cd docker
docker compose -f docker-compose.dev.yml up -d --build
```

### Stop

```bash
docker compose down
# with volumes: docker compose down -v
```

---

## Health checks

```bash
# Backend liveness
curl -fsS http://localhost:8080/actuator/health/liveness

# Backend readiness (includes DB)
curl -fsS http://localhost:8080/actuator/health

# Frontend
curl -fsS http://localhost:4200/health
```

Compose waits for DB → backend → frontend using `service_healthy` conditions.

---

## Authentication

- Access and refresh tokens are **httpOnly cookies** (`erm_access_token`, `erm_refresh_token`).
- Access TTL: **15 minutes** (default). Refresh TTL: **7 days**.
- Login: `POST /auth/login` with JSON body `{ "email", "password" }`.
- Refresh: `POST /auth/refresh` (refresh cookie required).
- Logout: `POST /auth/logout`.

See [AUTH_REFRESH.md](./AUTH_REFRESH.md) and [ADR-auth-session.md](./ADR-auth-session.md).

### Required production env

| Variable | Notes |
|----------|-------|
| `JWT_SECRET` | Min 32 characters; never commit |
| `ERM_ENCRYPTION_KEY` | Min 32 characters; encrypts MFA secrets at rest (separate from JWT) |
| `POSTGRES_PASSWORD` | Strong password |
| `CORS_ALLOWED_ORIGINS` | Exact frontend origin (credentials require explicit origin) |
| `AUTH_COOKIE_SECURE` | `true` in production |
| `ERM_DEMO_DATA_ENABLED` | `false` in production |
| `ERM_RATE_LIMIT_REQUESTS_PER_MINUTE` | Optional; default `10` per IP per auth endpoint |

---

## Logs

### Development

Plain-text logs with correlation ID in brackets:

```
2026-06-27 10:15:00.123 INFO [http-nio-8080-exec-1] [a1b2c3d4-...] c.c.erm.service.AuthService - ...
```

### Production (`spring.profiles.active=prod`)

JSON logs via Logstash encoder. Each line includes `requestId` when the request passed through the API.

```bash
# Tail backend logs in Docker
docker logs -f erm-backend

# Parse JSON locally
docker logs erm-backend 2>&1 | jq 'select(.requestId != null)'
```

### Correlation IDs

- Header: `X-Request-Id`
- Backend generates one if the client omits it; response echoes the same value.
- Angular sends `X-Request-Id` on every API request via `authInterceptor`.

---

## Metrics

Prometheus scrape endpoint (authenticated in prod):

```
GET /actuator/prometheus
```

See [backend/docs/monitoring/ACTUATOR_ENDPOINTS.md](../backend/docs/monitoring/ACTUATOR_ENDPOINTS.md).

---

## Common issues

### Login returns 401 after deploy

1. Confirm Flyway migrations ran (`V14__refresh_tokens.sql` for cookie auth).
2. Check `CORS_ALLOWED_ORIGINS` matches the browser origin exactly.
3. Verify `AUTH_COOKIE_SECURE` is not `true` over plain HTTP in dev.

### Login throttled (429)

Auth rate limit: **10 requests/minute per IP per endpoint** (default). Applies to `POST /auth/login`, `/auth/signup`, `/auth/forgot-password`, `/auth/mfa/verify`, and `/auth/refresh`. Override with `ERM_RATE_LIMIT_REQUESTS_PER_MINUTE`. Wait 60s or check `X-Forwarded-For` if behind a proxy.

### Dashboard shows stale data after org switch

Dashboard cache is evicted on org switch. If data still looks wrong, restart backend or wait for 5-minute cache TTL.

### Backend won't start — DB not ready

Ensure compose `depends_on: service_healthy` is used. Check `docker logs erm-db`.

### Demo user missing in dev

Set `ERM_DEMO_DATA_ENABLED=true` and ensure Flyway demo migrations run (`V3__seed_test_data.sql` + `V21__ideal_demo_user_pack.sql`).

**Demo personas** (password `password123` for all):

| Email | Role |
|-------|------|
| `admin@demo.com` | ORG_ADMIN |
| `hr@demo.com` | HR_ADMIN |
| `pm@demo.com` | PROJECT_MANAGER |
| `employee@demo.com` | EMPLOYEE |
| `manager@demo.com` | DEPARTMENT_MANAGER |

On the dev login page, expand **Demo Access** to one-click fill credentials.

---

## CI smoke test

GitHub Actions runs `docker/scripts/smoke-test.sh` after image build:

1. Stack up with health wait
2. Backend liveness
3. Demo login (`admin@demo.com` / `password123`; V21 adds four more personas — see RUNBOOK)
4. Frontend health

---

## Security testing

Run the manual matrix in [SECURITY_TEST_PLAN.md](./SECURITY_TEST_PLAN.md) before production exposure.

---

## Related docs

- [IMPLEMENTATION_PLAN.md](./IMPLEMENTATION_PLAN.md)
- [AUDIT_REPORT.md](./AUDIT_REPORT.md)
- [Backend monitoring](../backend/docs/monitoring/ACTUATOR_SETUP.md)
- [env.example](../env.example)
