#!/bin/sh
set -e

export API_BASE_URL="${API_BASE_URL:-http://localhost:8081}"
export MODULO="${MODULO:-m1}"
export FUNC_CITAS="${FUNC_CITAS:-true}"
export FUNC_TRAMITES="${FUNC_TRAMITES:-false}"
export FUNC_PAGOS="${FUNC_PAGOS:-false}"
export FUNC_BFF="${FUNC_BFF:-false}"
export FUNC_HISTORIAL="${FUNC_HISTORIAL:-false}"

envsubst '${API_BASE_URL} ${MODULO} ${FUNC_CITAS} ${FUNC_TRAMITES} ${FUNC_PAGOS} ${FUNC_BFF} ${FUNC_HISTORIAL}' \
  < /usr/share/nginx/html/assets/config.template.json \
  > /usr/share/nginx/html/assets/config.json

echo "[40-config.sh] Configuración generada en /usr/share/nginx/html/assets/config.json (MODULO=${MODULO}, API_BASE_URL=${API_BASE_URL})"

