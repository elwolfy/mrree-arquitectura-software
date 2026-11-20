#!/usr/bin/env bash
# ==============================================================================
# psql.sh — Abre una sesión psql dentro de m4-postgres como postgres sobre consular
# Uso interactivo: cd m3-datos && ./scripts/psql.sh
# Uso con comando: cd m3-datos && ./scripts/psql.sh -c "\dn"
# ==============================================================================

set -euo pipefail

if ! docker info >/dev/null 2>&1; then
  echo "[ERROR] Docker Desktop no está en ejecución o el demonio no responde." >&2
  exit 1
fi

ESTADO=$(docker inspect --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' m4-postgres 2>/dev/null || echo "no-encontrado")
if [ "$ESTADO" != "healthy" ]; then
  echo "[ERROR] El contenedor 'm4-postgres' no está activo o saludable (estado: ${ESTADO})." >&2
  echo "        Ejecute primero './scripts/levantar.sh'." >&2
  exit 1
fi

if [ $# -gt 0 ]; then
  exec docker exec -i m4-postgres psql -U postgres -d consular "$@"
else
  exec docker exec -it m4-postgres psql -U postgres -d consular
fi

