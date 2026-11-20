#!/usr/bin/env bash
# ==============================================================================
# detener.sh — Detiene los contenedores del Módulo 4 (Observabilidad)
# Uso: cd m4-observabilidad && ./scripts/detener.sh
# ==============================================================================

set -euo pipefail

if ! docker info >/dev/null 2>&1; then
  echo "[ERROR] Docker Desktop no está en ejecución o el demonio no responde." >&2
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULO_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${MODULO_DIR}"

echo "[M4 - Observabilidad] Deteniendo contenedores..."
if ! docker compose --profile sidecar --profile c4 --profile calidad down --remove-orphans; then
  echo "[ERROR] Falló al detener los contenedores con 'docker compose down'." >&2
  exit 1
fi
echo "[OK] Contenedores de M4 detenidos."
