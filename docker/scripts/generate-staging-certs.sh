#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CERT_DIR="${SCRIPT_DIR}/../certs"
DOMAIN="${1:-localhost}"

mkdir -p "${CERT_DIR}"

if command -v openssl >/dev/null 2>&1; then
  openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout "${CERT_DIR}/tls.key" \
    -out "${CERT_DIR}/tls.crt" \
    -subj "/CN=${DOMAIN}/O=ERM Staging/C=US"
else
  echo "openssl not found locally; generating certs via Docker..."
  docker run --rm -v "${CERT_DIR}:/certs" alpine/openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout /certs/tls.key \
    -out /certs/tls.crt \
    -subj "/CN=${DOMAIN}/O=ERM Staging/C=US"
fi

echo "Created ${CERT_DIR}/tls.crt and tls.key for CN=${DOMAIN}"
echo "Add https://${DOMAIN} to CORS_ALLOWED_ORIGINS in .env"

