#!/usr/bin/env bash
# ==============================================================================
# pagar-dos-veces.sh — Demuestra la idempotencia de POST /api/pagos con Idempotency-Key
# Uso: cd m3-datos && ./scripts/pagar-dos-veces.sh [TRAMITE_ID]
# ==============================================================================

set -euo pipefail

if ! docker info >/dev/null 2>&1; then
  echo "[ERROR] Docker Desktop no está en ejecución o el demonio no responde." >&2
  exit 1
fi

TRAMITE_ID="${1:-}"
GATEWAY_URL="${GATEWAY_URL:-http://localhost:8080}"
MONTO="33.00"

if [ -z "${TRAMITE_ID}" ]; then
  echo "=================================================================="
  echo " [1/3] Registrando trámite de prueba a través del Gateway..."
  echo "=================================================================="
  RESP_TRAMITE=$(curl -fsS -X POST "${GATEWAY_URL}/api/tramites" \
    -H "Content-Type: application/json" \
    -d '{"dni":"40000001","nombres":"Ana Perez","email":"ana@correo.lab","tipoTramite":"APOSTILLA","pais":"ES","folios":2,"idioma":"ES"}')
  TRAMITE_ID=$(echo "${RESP_TRAMITE}" | grep -oE '"id"\s*:\s*"[^"]+"' | head -n1 | cut -d'"' -f4)
  echo "  -> Trámite creado con id=${TRAMITE_ID}"
fi

IDEMPOTENCY_KEY=$(cat /proc/sys/kernel/random/uuid 2>/dev/null || uuidgen || echo "idem-$(date +%s)")

echo ""
echo "=================================================================="
echo " [2/3] Enviando DOS peticiones POST /api/pagos con la MISMA clave"
echo "       Idempotency-Key: ${IDEMPOTENCY_KEY}"
echo "=================================================================="

echo "--- Respuesta 1 (Primer intento) ---"
curl -fsS -X POST "${GATEWAY_URL}/api/pagos" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: ${IDEMPOTENCY_KEY}" \
  -d "{\"tramiteId\":\"${TRAMITE_ID}\",\"monto\":${MONTO}}"
echo ""

echo "--- Respuesta 2 (Reintento con misma Idempotency-Key) ---"
curl -fsS -X POST "${GATEWAY_URL}/api/pagos" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: ${IDEMPOTENCY_KEY}" \
  -d "{\"tramiteId\":\"${TRAMITE_ID}\",\"monto\":${MONTO}}"
echo ""

echo ""
echo "=================================================================="
echo " [3/3] Verificando conteo en pagos.pago para el trámite ${TRAMITE_ID}"
echo "=================================================================="
CONTEO=$(docker exec m4-postgres psql -U postgres -d consular -t -A -c "SELECT COUNT(*) FROM pagos.pago WHERE tramite_id = '${TRAMITE_ID}';" | tr -d '[:space:]')
echo "  -> Registros en pagos.pago para trámite ${TRAMITE_ID}: ${CONTEO}"

if [ "${CONTEO}" != "1" ]; then
  echo "[ERROR] Se esperaba exactamente 1 registro en pagos.pago, pero se encontraron ${CONTEO}." >&2
  exit 1
fi
echo "  [OK] Idempotencia verificada: un solo cobro registrado y misma respuesta devuelta."

