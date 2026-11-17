#!/usr/bin/env bash
# ==============================================================================
# medir.sh - Mide tiempos de arranque en logs y uso de memoria RAM en M1
# Uso: ./scripts/medir.sh
# ==============================================================================

set -uo pipefail

if ! docker info >/dev/null 2>&1; then
  echo "Docker Desktop no está en ejecución: ábralo y espere 'Engine running'"
  exit 1
fi

mapfile -t CONTENEDORES < <(docker ps --filter "name=^m1-" --format "{{.Names}}")

if [ "${#CONTENEDORES[@]}" -eq 0 ]; then
  echo "[AVISO] No hay contenedores 'm1-*' en ejecución actualmente."
  exit 0
fi

echo "=================================================================="
echo " 1. Tiempos de Arranque Registrados en Logs (M1)"
echo "=================================================================="

for NOMBRE in "${CONTENEDORES[@]}"; do
  LOGS=$(docker logs "$NOMBRE" 2>&1 || true)
  LINEA_FUNC=$(echo "$LOGS" | grep -Eo "Función lista en [0-9]+ ms" | tail -n 1 || true)
  LINEA_SPRING=$(echo "$LOGS" | grep -Eo "Started [^ ]+ in [0-9.]+ seconds" | tail -n 1 || true)

  if [ -n "$LINEA_FUNC" ]; then
    printf "  * %-20s: %s\n" "$NOMBRE" "$LINEA_FUNC"
  elif [ -n "$LINEA_SPRING" ]; then
    printf "  * %-20s: %s\n" "$NOMBRE" "$LINEA_SPRING"
  else
    printf "  * %-20s: (servicio de infraestructura / sin métrica JVM)\n" "$NOMBRE"
  fi
done

echo ""
echo "=================================================================="
echo " 2. Uso de Recursos en Tiempo Real (docker stats --no-stream)"
echo "=================================================================="

docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}" "${CONTENEDORES[@]}"

TOTAL_MIB=$(docker stats --no-stream --format "{{.MemUsage}}" "${CONTENEDORES[@]}" | awk -F'/' '{
  val=$1; gsub(/^[ \t]+|[ \t]+$/, "", val);
  if (val ~ /GiB$/) { sub(/GiB$/, "", val); sum += val * 1024 }
  else if (val ~ /MiB$/) { sub(/MiB$/, "", val); sum += val }
  else if (val ~ /KiB$/) { sub(/KiB$/, "", val); sum += val / 1024 }
} END { printf "%.2f", sum }')

echo "------------------------------------------------------------------"
echo " Número de contenedores activos (m1-*): ${#CONTENEDORES[@]}"
echo " Memoria total consumida (aprox.):      ${TOTAL_MIB} MiB"
echo "=================================================================="

