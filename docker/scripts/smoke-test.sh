#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

if [[ ! -f .env ]]; then
  cat > .env <<'EOF'
POSTGRES_DB=erm_db
POSTGRES_USER=erm_user
POSTGRES_PASSWORD=ci_smoke_pass
JWT_SECRET=ci-smoke-jwt-secret-minimum-32-chars
JWT_EXPIRATION=86400000
ERM_DEMO_DATA_ENABLED=true
CORS_ALLOWED_ORIGINS=http://localhost:4200
EOF
fi

cleanup() {
  docker compose -f docker-compose.yml -f docker-compose.ci.yml down -v --remove-orphans || true
}
trap cleanup EXIT

echo "Starting CI stack..."
if ! docker compose -f docker-compose.yml -f docker-compose.ci.yml up -d --build --wait --timeout 300; then
  echo "Stack failed to become healthy. Backend logs:" >&2
  docker compose -f docker-compose.yml -f docker-compose.ci.yml logs backend --tail 120 >&2 || true
  exit 1
fi

echo "Backend liveness..."
curl -fsS http://localhost:8080/actuator/health/liveness | grep -q '"status":"UP"'

echo "Demo login..."
login_status=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@demo.com","password":"password123"}')
if [[ "${login_status}" != "200" ]]; then
  echo "Expected login HTTP 200, got ${login_status}" >&2
  docker compose -f docker-compose.yml -f docker-compose.ci.yml logs backend --tail 80 >&2 || true
  exit 1
fi

echo "Frontend health..."
curl -fsS http://localhost:4200/health | grep -q ok

echo "All smoke checks passed."
