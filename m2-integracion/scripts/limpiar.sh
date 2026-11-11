#!/usr/bin/env bash
# ==============================================================================
# limpiar.sh — Elimina contenedores, redes, volúmenes y huérfanos del Módulo 2
# Uso: cd m2-integracion && ./scripts/limpiar.sh
# ==============================================================================

set -euo pipefail

# Verificar con 'docker info' que Docker Desktop está en ejecución
if ! docker info >/dev/null 2>&1; then
  echo "[ERROR] Docker Desktop no está en ejecución o el demonio no responde." >&2
  echo "        Inicie Docker Desktop, espere a que el motor esté listo y vuelva a ejecutar este script." >&2
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULO_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${MODULO_DIR}"

echo "[M2 - Integración] Eliminando contenedores, redes, volúmenes y huérfanos..."
docker compose down -v --remove-orphans
echo "[OK] Recursos de M2 eliminados completamente."

