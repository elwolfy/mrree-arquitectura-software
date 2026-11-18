#!/usr/bin/env bash
# ==============================================================================
# limpiar.sh — Elimina contenedores, redes, volúmenes y huérfanos del Módulo 3
# Uso: cd m3-datos && ./scripts/limpiar.sh [--forzar]
# ==============================================================================

set -euo pipefail

if ! docker info >/dev/null 2>&1; then
  echo "[ERROR] Docker Desktop no está en ejecución o el demonio no responde." >&2
  echo "        Inicie Docker Desktop, espere a que el motor esté listo y vuelva a ejecutar este script." >&2
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULO_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${MODULO_DIR}"

echo "[M3 - Datos] Eliminando contenedores, redes, volúmenes y huérfanos..."
if ! docker compose --profile sidecar down -v --remove-orphans; then
  echo "[ERROR] Falló al ejecutar 'docker compose down -v --remove-orphans'." >&2
  exit 1
fi
echo "[OK] Recursos de M3 eliminados completamente."

