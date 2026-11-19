#!/usr/bin/env bash
# ==============================================================================
# cargar-datos-masivos.sh — Carga 1 000 000 de filas en tramites.vista_tramite
# Uso: cd m3-datos && ./scripts/cargar-datos-masivos.sh
# ==============================================================================

set -euo pipefail

if ! docker info >/dev/null 2>&1; then
  echo "[ERROR] Docker Desktop no está en ejecución o el demonio no responde." >&2
  exit 1
fi

ESTADO=$(docker inspect --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' m3-postgres 2>/dev/null || echo "no-encontrado")
if [ "$ESTADO" != "healthy" ]; then
  echo "[ERROR] El contenedor 'm3-postgres' no está activo o saludable (estado: ${ESTADO})." >&2
  echo "        Ejecute primero './scripts/levantar.sh'." >&2
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
SQL_FILE="${REPO_ROOT}/docs/pruebas/sql/carga-masiva.sql"

echo "=================================================================="
echo " [M3 - Optimización] Cargando 1 000 000 de filas en vista_tramite..."
echo " Archivo: ${SQL_FILE}"
echo "=================================================================="

INICIO=$(date +%s)
docker exec -i m3-postgres psql -U postgres -d consular -v ON_ERROR_STOP=1 < "${SQL_FILE}"
FIN=$(date +%s)
DURACION=$((FIN - INICIO))

echo ""
echo "[OK] Carga masiva de 1 000 000 de filas completada en ${DURACION} segundos."

