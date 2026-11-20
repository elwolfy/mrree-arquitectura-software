#!/usr/bin/env bash
# ==============================================================================
# validar-apis.sh — Valida los contratos OpenAPI de los servicios con Spectral
# Uso: cd m4-observabilidad && ./scripts/validar-apis.sh [ARCHIVO_CONTRATO]
# ==============================================================================

set -euo pipefail

if ! docker info >/dev/null 2>&1; then
  echo "[ERROR] Docker Desktop no está en ejecución o el demonio no responde." >&2
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
ESTANDARES_DIR="${REPO_ROOT}/docs/estandares"

if [ $# -ge 1 ] && [ -n "$1" ]; then
  echo "=================================================================="
  echo " [M4 - Gobernanza de APIs] Validando contrato específico: $1"
  echo "=================================================================="
  docker run --rm -v "${REPO_ROOT}:/work" -w /work stoplight/spectral:6.11.0 lint "$1" --ruleset /work/docs/estandares/.spectral.yaml --fail-severity=error
  exit $?
fi

echo "=================================================================="
echo " [M4 - Gobernanza de APIs] Validando contratos OpenAPI con Spectral"
echo " Reglas: docs/estandares/.spectral.yaml (rutas-en-kebab-case)"
echo "=================================================================="

docker run --rm -v "${ESTANDARES_DIR}:/work" -w /work stoplight/spectral:6.11.0 lint "/work/openapi/*.json" --ruleset /work/.spectral.yaml --fail-severity=error
echo ""
echo "[OK] Todos los contratos OpenAPI cumplen los lineamientos de docs/estandares/.spectral.yaml (0 errores)."

