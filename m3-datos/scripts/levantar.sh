#!/usr/bin/env bash
# ==============================================================================
# levantar.sh — Construye, levanta y espera los healthchecks del Módulo 3 (Datos)
# Uso: cd m3-datos && ./scripts/levantar.sh
# ==============================================================================

set -euo pipefail

# Verificar con 'docker info' que Docker Desktop está en ejecución
if ! docker info >/dev/null 2>&1; then
  echo "[ERROR] Docker Desktop no está en ejecución o el demonio no responde." >&2
  echo "        Inicie Docker Desktop, espere a que el motor esté listo y vuelva a ejecutar este script." >&2
  exit 1
fi

# Verificar si hay contenedores activos de M1 o M2 ocupando los puertos 8080, 8081 o 4200
CONFLICTOS_M1_M2=$(docker ps --format '{{.Names}}' | grep -E '^(m1-|m2-)' || true)
if [ -n "${CONFLICTOS_M1_M2}" ]; then
  echo "[ERROR] Los puertos 8080, 8081 o 4200 están ocupados por contenedores activos de M1 o M2:" >&2
  echo "${CONFLICTOS_M1_M2}" | sed 's/^/        - /' >&2
  echo "        Detenga primero el módulo anterior (p. ej. './m1-estilos/scripts/detener.sh' o './m2-integracion/scripts/detener.sh') antes de levantar M3." >&2
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULO_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${MODULO_DIR}"

echo "=================================================================="
echo " [M3 - Datos] Construyendo y levantando contenedores..."
echo "=================================================================="

if ! docker compose up -d --build --remove-orphans; then
  echo "[ERROR] Falló al construir o iniciar los contenedores con 'docker compose up'." >&2
  exit 1
fi

CONTENEDORES=("m3-postgres" "m3-redis" "m3-rabbitmq" "m3-mailpit" "m3-pide" "m3-citas" "m3-tramites" "m3-pagos" "m3-notificaciones" "m3-identidad" "m3-bff" "m3-gateway" "m3-frontend")
TIEMPO_MAXIMO_SEGUNDOS=120
INTERVALO_SEGUNDOS=2

echo ""
echo "Esperando a que los contenedores alcancen el estado 'healthy' (máximo ${TIEMPO_MAXIMO_SEGUNDOS}s por contenedor)..."

for nombre in "${CONTENEDORES[@]}"; do
  transcurrido=0
  estado="no-encontrado"

  while [ "$transcurrido" -lt "$TIEMPO_MAXIMO_SEGUNDOS" ]; do
    estado=$(docker inspect --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$nombre" 2>/dev/null || echo "no-encontrado")
    if [ "$estado" = "healthy" ]; then
      echo "  [OK] ${nombre} -> healthy"
      break
    fi
    sleep "$INTERVALO_SEGUNDOS"
    transcurrido=$((transcurrido + INTERVALO_SEGUNDOS))
  done

  if [ "$estado" != "healthy" ]; then
    echo "[ERROR] Tiempo máximo de espera agotado (${TIEMPO_MAXIMO_SEGUNDOS}s) para el contenedor '${nombre}' (último estado: ${estado})." >&2
    exit 1
  fi
done

docker exec m3-postgres psql -U postgres -d consular -q -f /docker-entrypoint-initdb.d/01-esquemas.sql >/dev/null 2>&1 || true

echo ""
echo "=================================================================="
echo " Servicios activos — Módulo 3 (Gestión de Datos y Consistencia)"
echo "=================================================================="
echo "  * Frontend Ciudadano (Modo M3):      http://localhost:4200"
echo "  * API Gateway (Entrada única):       http://localhost:8080"
echo "  * Swagger UI Agregado (Gateway):     http://localhost:8080/swagger-ui.html"
echo "  * bff-web (Swagger UI):              http://localhost:8085/swagger-ui.html"
echo "  * citas-service (Swagger UI):        http://localhost:8081/swagger-ui.html"
echo "  * tramites-service (Swagger UI):     http://localhost:8082/swagger-ui.html"
echo "  * tramites-service (gRPC):           localhost:9082"
echo "  * pagos-service (Swagger UI):        http://localhost:8083/swagger-ui.html"
echo "  * notificaciones-service (Swagger):  http://localhost:8084/swagger-ui.html"
echo "  * identidad-service (Swagger UI):    http://localhost:8086/swagger-ui.html"
echo "  * Simulador PIDE (WireMock):         http://localhost:8089/__admin/mappings"
echo "  * RabbitMQ Consola Web:              http://localhost:15672  (usuario: consular / clave: consular)"
echo "  * Mailpit Web UI:                    http://localhost:8025"
echo "  * Redis:                             localhost:6379"
echo "  * PostgreSQL:                        localhost:5432          (BD: consular)"
echo "=================================================================="

