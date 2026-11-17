#!/usr/bin/env bash
# ==============================================================================
# simular-particion.sh - Desconecta la red m1_datos de m1-monolito o m1-clean
# Uso: ./scripts/simular-particion.sh
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
  echo "        Levante primero una de esas variantes con: ./scripts/levantar.sh monolito"
  exit 1
fi

echo "[M1 - CAP] Simulando partición de red: desconectando '${CONTENEDOR_OBJETIVO}' de la red 'm1_datos'..."
if ! docker network disconnect m1_datos "$CONTENEDOR_OBJETIVO"; then
  echo "[AVISO] No se pudo desconectar '${CONTENEDOR_OBJETIVO}' de 'm1_datos' (puede que ya esté desconectado)."
  exit 1
fi

echo "[OK] '${CONTENEDOR_OBJETIVO}' desconectado de 'm1_datos' (PostgreSQL inaccesible)."
echo "     Observe en http://localhost:4200 o en /api/oficinas/MAD/disponibilidad el comportamiento CP vs AP."
echo "     Para restaurar la conexión ejecute: ./scripts/restaurar-red.sh"

