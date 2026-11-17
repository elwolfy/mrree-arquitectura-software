#!/usr/bin/env bash
# ==============================================================================
# carga.sh - Ejecuta pruebas de carga k6 sobre la variante activa de M1 (:8081)
# Uso: ./scripts/carga.sh [citas|pico]
# ==============================================================================

set -uo pipefail

ESCENARIO="${1:-citas}"

case "$ESCENARIO" in
  citas) ARCHIVO_SCRIPT="citas.js" ;;
  pico)  ARCHIVO_SCRIPT="citas-pico.js" ;;
  *)
    echo "[ERROR] Escenario no válido: '$ESCENARIO'. Use: citas | pico"
    exit 1
    ;;
esac

if ! docker info >/dev/null 2>&1; then
  echo "Docker Desktop no está en ejecución: ábralo y espere 'Engine running'"
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULO_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "$MODULO_DIR"

RUTA_LOCAL="../docs/pruebas/k6/${ARCHIVO_SCRIPT}"
if [ ! -f "$RUTA_LOCAL" ]; then
  echo "[ERROR] No se encontró el script k6 en '${RUTA_LOCAL}'."
  exit 1
fi

export K6_SCRIPT="$ARCHIVO_SCRIPT"
export OBJETIVO="${OBJETIVO:-http://host.docker.internal:8081}"

echo "=================================================================="
echo " [M1 - Carga k6] Ejecutando '${K6_SCRIPT}' contra ${OBJETIVO}..."
echo "=================================================================="

if ! docker compose --profile carga run --rm k6; then
  echo "[ERROR] La ejecución de k6 ('${ARCHIVO_SCRIPT}') finalizó con errores."
  exit 1
fi

