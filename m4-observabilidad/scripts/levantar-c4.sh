#!/usr/bin/env bash
# ==============================================================================
# levantar-c4.sh — Levanta solo el contenedor m4-structurizr (puerto 8090)
# Uso: cd m4-observabilidad && ./scripts/levantar-c4.sh
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

echo "=================================================================="
echo " [M4 - Modelo C4] Levantando Structurizr en http://localhost:8090..."
echo "=================================================================="

if ! docker compose --profile c4 up -d structurizr; then
  echo "[ERROR] Falló al iniciar el contenedor m4-structurizr." >&2
  exit 1
fi

TIEMPO_MAXIMO_SEGUNDOS=60
INTERVALO_SEGUNDOS=2
transcurrido=0
estado="no-encontrado"

while [ "$transcurrido" -lt "$TIEMPO_MAXIMO_SEGUNDOS" ]; do
  estado=$(docker inspect --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' m4-structurizr 2>/dev/null || echo "no-encontrado")
  if [ "$estado" = "healthy" ]; then
    echo "  [OK] m4-structurizr (Modelo C4 en docs/c4/workspace.dsl) -> healthy"
    break
  fi
  sleep "$INTERVALO_SEGUNDOS"
  transcurrido=$((transcurrido + INTERVALO_SEGUNDOS))
done

if [ "$estado" != "healthy" ]; then
  echo "[ERROR] Tiempo máximo agotado (${TIEMPO_MAXIMO_SEGUNDOS}s) esperando a m4-structurizr (estado: ${estado})." >&2
  exit 1
fi

echo ""
echo "=================================================================="
echo " Structurizr activo: http://localhost:8090"
echo " Workspace DSL:      docs/c4/workspace.dsl"
echo "=================================================================="

