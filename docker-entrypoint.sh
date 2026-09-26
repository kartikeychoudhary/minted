#!/bin/sh
set -e

# DNS server nginx uses to re-resolve BACKEND_URL at runtime.
# Defaults to the container's own nameserver (Docker's embedded DNS on compose networks).
if [ -z "${NGINX_RESOLVER}" ]; then
  NGINX_RESOLVER=$(awk '/^nameserver/ { print $2; exit }' /etc/resolv.conf)
  case "${NGINX_RESOLVER}" in
    *:*) NGINX_RESOLVER="[${NGINX_RESOLVER}]" ;;  # IPv6 nameservers need brackets
  esac
fi
export NGINX_RESOLVER="${NGINX_RESOLVER:-127.0.0.11}"

# Substitute environment variables in nginx config
envsubst '${BACKEND_URL} ${NGINX_RESOLVER}' < /etc/nginx/conf.d/default.conf.template > /etc/nginx/conf.d/default.conf

# Start nginx
exec "$@"
