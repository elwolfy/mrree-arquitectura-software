#!/usr/bin/env bash
# ==============================================================================
# detener.sh - Detiene todos los contenedores del Modulo 1 sin borrar datos
# Uso: ./scripts/detener.sh
# ==============================================================================

set -uo pipefail

if ! docker info >/dev/null 2>&1; then
  echo "Docker Desktop no está en ejecución: ábralo y espere 'Engine running'"
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULO_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "$MODULO_DIR"

echo "[M1 - Estilos] Deteniendo todos los contenedores activos (conservando datos)..."
if ! docker compose --profile "*" stop; then
  echo "[ERROR] 'docker compose --profile \"*\" stop' devolvió un error."
  exit 1
fi
echo "[OK] Contenedores del Módulo 1 detenidos."

