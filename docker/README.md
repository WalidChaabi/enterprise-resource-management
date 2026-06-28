# Docker deployment

## Production (HTTP)

```bash
cd docker
cp env.example .env
# Edit .env: POSTGRES_PASSWORD, JWT_SECRET, CORS_ALLOWED_ORIGINS
docker compose up --build
```

- Frontend: http://localhost:4200 (mapped to nginx port 80)
- Backend API (direct): http://localhost:8080
- Health: `curl http://localhost:8080/actuator/health`

## Production with TLS (staging / HTTPS)

TLS terminates at the **frontend nginx** container. The backend is not exposed publicly when using the TLS overlay.

### 1. Generate staging certificates

**Linux/macOS:**

```bash
cd docker/scripts
chmod +x generate-staging-certs.sh
./generate-staging-certs.sh localhost
```

**Windows (PowerShell):**

```powershell
cd docker\scripts
.\generate-staging-certs.ps1 -Domain localhost
```

Certificates are written to `docker/certs/` (`tls.crt`, `tls.key`). For real production, replace these with certificates from your CA (Let's Encrypt, etc.).

### 2. Configure environment

In `docker/.env`:

```env
CORS_ALLOWED_ORIGINS=https://localhost
POSTGRES_PASSWORD=...
JWT_SECRET=...
```

`CORS_ALLOWED_ORIGINS` must match the browser origin exactly (scheme + host + port, no trailing slash).

### 3. Start with TLS overlay

```bash
cd docker
docker compose -f docker-compose.yml -f docker-compose.tls.yml up --build
```

- HTTPS: https://localhost (accept self-signed cert in browser for staging)
- HTTP port 80 redirects to HTTPS (except `/health` for probes)

### Reverse proxy headers

nginx sets `X-Forwarded-For`, `X-Forwarded-Proto`, `X-Forwarded-Host`, and `X-Forwarded-Port` on `/auth/` and `/api/` requests. Spring Boot prod profile uses `server.forward-headers-strategy=framework` so the app sees the client scheme and host correctly.

### CI/CD

- **`.github/workflows/ci.yml`** — unit tests, lint, OWASP Dependency-Check, `npm audit`
- **`.github/workflows/docker-build.yml`** — build/push images to GHCR (`ghcr.io/<owner>/<repo>/erm-backend|erm-front`) with SHA tags, then run `docker/scripts/smoke-test.sh`

Images on `main`/`master` push:

```
ghcr.io/<owner>/<repo>/erm-backend:<git-sha>
ghcr.io/<owner>/<repo>/erm-front:<git-sha>
```


Mount your CA-issued files as `docker/certs/tls.crt` and `docker/certs/tls.key`, or bind-mount from `/etc/letsencrypt/live/your-domain/`.

Update `CORS_ALLOWED_ORIGINS` to your public URL, e.g. `https://app.example.com`.

## Development stack

```bash
cd docker
docker compose -f docker-compose.dev.yml up --build
```

- Frontend (hot reload): http://localhost:4200
- Backend API: http://localhost:8080
- pgAdmin: http://localhost:5050 (admin@admin.com / admin)
- Demo login: `admin@demo.com` / `password123`

Dev uses a **separate** Postgres volume (`erm-platform-pgdata-dev`) with password `erm_pass`. Do not mix with production compose.

## Troubleshooting

### Backend keeps restarting (`password authentication failed`)

Postgres stores the password only when the data volume is **first created**. If you change `POSTGRES_PASSWORD` in `.env` later, the DB still expects the old password.

**Fix:** reset the volume for that stack (destroys DB data):

```bash
# Production
docker compose down -v

# Development
docker compose -f docker-compose.dev.yml down -v
```

Then `up --build` again. Dev and prod use different volume names so they no longer clash.

### `erm-front` stuck in `Created`

The frontend waits for a healthy backend. Check `docker logs erm-backend` and fix DB/JWT env vars first.

### Rebuild after dependency changes

```bash
docker compose -f docker-compose.dev.yml build --no-cache front backend
docker compose -f docker-compose.dev.yml up -d
```

### Flyway errors after moving demo migrations

The dev backend bind-mounts `backend/`. Stale files under `backend/target/classes` can confuse Flyway. Run:

```bash
cd backend && ./mvnw clean
docker compose -f docker-compose.dev.yml restart backend
```
