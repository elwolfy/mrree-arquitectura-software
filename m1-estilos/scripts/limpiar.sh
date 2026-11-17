#!/usr/bin/env bash
# ==============================================================================
# limpiar.sh - Elimina contenedores, redes, volumenes y huerfanos del Modulo 1
# Uso: ./scripts/limpiar.sh [--forzar]
# ==============================================================================

set -uo pipefail

FORZAR=false
if [ "${1:-}" = "--forzar" ] || [ "${1:-}" = "-f" ]; then
  FORZAR=true
fi

if ! docker info >/dev/null 2>&1; then
  echo "Docker Desktop no está en ejecución: ábralo y espere 'Engine running'"
  exit 1
fi

if [ "$FORZAR" = false ]; then
  read -r -p "¿Está seguro de eliminar todos los contenedores y volúmenes de datos de M1? (s/N): " RESPUESTA
  case "$RESPUESTA" in
    s|S|si|SI|sí|SÍ|y|Y|yes|YES) ;;
    *)
      echo "Operación cancelada por el usuario."
      exit 0
      ;;
  esac
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULO_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "$MODULO_DIR"

echo "[M1 - Estilos] Eliminando contenedores, redes y volúmenes de datos..."
if ! docker compose --profile "*" down -v --remove-orphans; then
  echo "[ERROR] 'docker compose --profile \"*\" down -v --remove-orphans' devolvió un error."
  exit 1
fi
echo "[OK] Entorno del Módulo 1 limpiado por completo."

