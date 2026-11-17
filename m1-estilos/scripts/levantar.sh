#!/usr/bin/env bash
# ==============================================================================
# levantar.sh - Construye, levanta y espera los healthchecks del Modulo 1
# Uso: ./scripts/levantar.sh [monolito|clean|microservicios|serverless]
# ==============================================================================

set -uo pipefail

VARIANTE="${1:-monolito}"

case "$VARIANTE" in
  monolito|clean|microservicios|serverless) ;;
  *)
    echo "[ERROR] Variante no válida: '$VARIANTE'. Use: monolito | clean | microservicios | serverless"
    exit 1
    ;;
esac

# a) Verificar con 'docker info' que Docker esta en ejecucion
if ! docker info >/dev/null 2>&1; then
  echo "Docker Desktop no está en ejecución: ábralo y espere 'Engine running'"
  exit 1
fi

# d) Detectar si el puerto 8081 o 4200 esta ocupado por otro modulo
for PUERTO in 8081 4200; do
  OCUPANTE=$(docker ps --filter "publish=${PUERTO}" --format "{{.Names}}" 2>/dev/null | grep -v "^m1-" | head -n 1 || true)
  if [ -n "$OCUPANTE" ]; then
    echo "[ERROR] El puerto ${PUERTO} está ocupado por el contenedor '${OCUPANTE}' de otro módulo."
    echo "        Deténgalo antes de continuar ejecutando:"
    echo "        ../m2-integracion/scripts/detener.sh  (o ..\\m2-integracion\\scripts\\detener.ps1 en PowerShell)"
    exit 1
  fi
done

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULO_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "$MODULO_DIR"

echo "=================================================================="
echo " [M1 - Estilos] Deteniendo variantes previas y levantando '${VARIANTE}'..."
echo "=================================================================="

docker rm -f m1-monolito m1-clean m1-citas m1-notificaciones m1-fn-consultas m1-fn-reservar m1-fn-notificar m1-enrutador >/dev/null 2>&1 || true

if docker ps --filter "name=^m1-postgres$" --format "{{.Names}}" 2>/dev/null | grep -q "^m1-postgres$"; then
  docker exec m1-postgres psql -U postgres -d consular -q -c "DROP SCHEMA IF EXISTS citas, tramites, notificaciones CASCADE;" >/dev/null 2>&1 || true
  docker exec m1-postgres psql -U postgres -d consular -q -f /docker-entrypoint-initdb.d/01-esquemas.sql >/dev/null 2>&1 || true
fi

# b) Ejecutar docker compose up -d --build
if ! docker compose --profile "$VARIANTE" up -d --build; then
  echo "[ERROR] 'docker compose --profile ${VARIANTE} up -d --build' devolvió un error. Revise la salida anterior."
  exit 1
fi

CONTENEDORES=("m1-postgres" "m1-mailpit" "m1-frontend")
case "$VARIANTE" in
  monolito)       CONTENEDORES+=("m1-monolito") ;;
  clean)          CONTENEDORES+=("m1-clean") ;;
  microservicios) CONTENEDORES+=("m1-notificaciones" "m1-citas") ;;
  serverless)     CONTENEDORES+=("m1-fn-notificar" "m1-fn-consultas" "m1-fn-reservar" "m1-enrutador") ;;
esac

TIEMPO_MAXIMO=180
INTERVALO=2

echo ""
echo "Esperando a que los contenedores alcancen el estado 'healthy' (máximo ${TIEMPO_MAXIMO} s)..."

for NOMBRE in "${CONTENEDORES[@]}"; do
  TRANSCURRIDO=0
  ESTADO="no-encontrado"

  while [ "$TRANSCURRIDO" -lt "$TIEMPO_MAXIMO" ]; do
    ESTADO=$(docker inspect --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$NOMBRE" 2>/dev/null || echo "no-encontrado")
    if [ "$ESTADO" = "healthy" ]; then
      printf "  [OK] %-20s -> healthy\n" "$NOMBRE"
      break
    fi
    sleep "$INTERVALO"
    TRANSCURRIDO=$((TRANSCURRIDO + INTERVALO))
  done

  if [ "$ESTADO" != "healthy" ]; then
    echo "[ERROR] El contenedor '${NOMBRE}' no alcanzó el estado 'healthy' en ${TIEMPO_MAXIMO} s (estado actual: ${ESTADO})."
    echo "        Inspeccione su registro de eventos con: docker logs ${NOMBRE}"
    exit 1
  fi
done

sleep 2

echo ""
echo "=================================================================="
echo " Servicios activos - Módulo 1 (Variante: ${VARIANTE})"
echo "=================================================================="
echo "  * Frontend Ciudadano: http://localhost:4200"
echo "  * API Principal:      http://localhost:8081"
if [ "$VARIANTE" = "serverless" ]; then
  echo "  * Swagger UI:         No aplica en la variante serverless (funciones expuestas tras enrutador Nginx)"
else
  echo "  * Swagger UI:         http://localhost:8081/swagger-ui.html"
fi
echo "  * Mailpit Web:        http://localhost:8025"
echo "=================================================================="
