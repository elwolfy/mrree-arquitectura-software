#!/usr/bin/env bash
# ==============================================================================
# detener.sh — Detiene los contenedores del Módulo 2 y limpia los volúmenes
# Uso: cd m2-integracion && ./scripts/detener.sh
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULO_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${MODULO_DIR}"

echo "[M2 - Integración] Deteniendo contenedores y eliminando volúmenes..."
docker compose down -v --remove-orphans
echo "[OK] Entorno de M2 detenido y limpio."
