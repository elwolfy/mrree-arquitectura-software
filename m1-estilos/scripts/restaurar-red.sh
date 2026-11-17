#!/usr/bin/env bash
# ==============================================================================
# restaurar-red.sh - Reconecta la red m1_datos a m1-monolito o m1-clean
# Uso: ./scripts/restaurar-red.sh
# ==============================================================================

set -uo pipefail

if ! docker info >/dev/null 2>&1; then
  echo "Docker Desktop no está en ejecución: ábralo y espere 'Engine running'"
  exit 1
fi

CONTENEDOR_OBJETIVO=""
for CANDIDATO in "m1-monolito" "m1-clean"; do
  EN_EJECUCION=$(docker ps --filter "name=^${CANDIDATO}$" --format "{{.Names}}" 2>/dev/null || true)
  if [ -n "$EN_EJECUCION" ]; then
    CONTENEDOR_OBJETIVO="$CANDIDATO"
    break
  fi
done

if [ -z "$CONTENEDOR_OBJETIVO" ]; then
  echo "[ERROR] Ni 'm1-monolito' ni 'm1-clean' están en ejecución actualmente."
  exit 1
fi

echo "[M1 - CAP] Restaurando conexión de red: conectando '${CONTENEDOR_OBJETIVO}' a la red 'm1_datos'..."
if ! docker network connect m1_datos "$CONTENEDOR_OBJETIVO"; then
  echo "[AVISO] No se pudo conectar '${CONTENEDOR_OBJETIVO}' a 'm1_datos' (puede que ya esté conectado)."
  exit 1
fi

echo "[OK] '${CONTENEDOR_OBJETIVO}' reconectado exitosamente a 'm1_datos'."

