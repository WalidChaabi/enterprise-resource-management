#!/bin/sh
set -eu

if [ "${NGINX_USE_TLS:-false}" = "true" ]; then
  if [ ! -f /etc/nginx/certs/tls.crt ] || [ ! -f /etc/nginx/certs/tls.key ]; then
    echo "NGINX_USE_TLS=true but /etc/nginx/certs/tls.crt or tls.key is missing" >&2
    exit 1
  fi
  cp /etc/nginx/nginx.ssl.conf /etc/nginx/conf.d/default.conf
  echo "nginx: TLS enabled (443)"
else
  cp /etc/nginx/nginx.http.conf /etc/nginx/conf.d/default.conf
  echo "nginx: HTTP only (80)"
fi

exec nginx -g 'daemon off;'
