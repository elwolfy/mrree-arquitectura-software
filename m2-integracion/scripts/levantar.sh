#!/usr/bin/env bash
# ==============================================================================
# levantar.sh — Construye, levanta y espera los healthchecks del Módulo 2
# Uso: cd m2-integracion && ./scripts/levantar.sh
# ==============================================================================

set -euo pipefail

# Verificar con 'docker info' que Docker Desktop está en ejecución
if ! docker info >/dev/null 2>&1; then
  echo "[ERROR] Docker Desktop no está en ejecución o el demonio no responde." >&2
  echo "        Inicie Docker Desktop, espere a que el motor esté listo y vuelva a ejecutar este script." >&2
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULO_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${MODULO_DIR}"

echo "=================================================================="
echo " [M2 - Integración] Construyendo y levantando contenedores..."
echo "=================================================================="

if ! docker compose up -d --build --remove-orphans; then
  echo "[ERROR] Falló al construir o iniciar los contenedores con 'docker compose up'." >&2
  exit 1
fi

CONTENEDORES=("m2-postgres" "m2-rabbitmq" "m2-mailpit" "m2-pide" "m2-citas" "m2-tramites" "m2-pagos" "m2-notificaciones" "m2-identidad" "m2-bff" "m2-gateway" "m2-frontend")
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

echo ""
echo "=================================================================="
echo " Servicios activos — Módulo 2 (Integración y Comunicación)"
echo "=================================================================="
echo "  * Frontend Ciudadano (Modo M2):      http://localhost:4200"
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
echo "  * PostgreSQL:                        localhost:5432          (BD: consular)"
echo "=================================================================="
