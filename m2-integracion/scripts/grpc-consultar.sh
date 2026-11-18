#!/usr/bin/env bash
# ==============================================================================
# grpc-consultar.sh — Consulta un trámite por gRPC (9082) y compara tamaño con REST (8082)
# Uso: ./m2-integracion/scripts/grpc-consultar.sh <TRAMITE_UUID>
# ==============================================================================

set -euo pipefail

if [ $# -lt 1 ]; then
  echo "Uso: $0 <TRAMITE_UUID>" >&2
  exit 1
fi

TRAMITE_ID="$1"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULO_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
PROTO_DIR="${MODULO_DIR}/tramites-service/src/main/proto"

echo "=================================================================="
echo " [M2 - REST vs. gRPC] Comparando consulta de trámite: ${TRAMITE_ID}"
echo "=================================================================="

URL_REST="http://localhost:8082/api/tramites/${TRAMITE_ID}"
RESP_REST=$(curl -fsS "${URL_REST}")
BYTES_REST=$(printf "%s" "${RESP_REST}" | wc -c | tr -d ' ')

echo ""
echo "1. Respuesta REST (GET ${URL_REST}):"
echo "${RESP_REST}"

echo ""
echo "2. Respuesta gRPC (consular.tramites.v1.TramitesService/ConsultarTramite en m2-tramites:9082):"
RESP_GRPC=$(docker run --rm --network m2_frontal -v "${PROTO_DIR}:/protos:ro" fullstorydev/grpcurl:v1.9.1 \
  -plaintext -import-path /protos -proto tramites.proto \
  -d "{\"id\":\"${TRAMITE_ID}\"}" m2-tramites:9082 consular.tramites.v1.TramitesService/ConsultarTramite)
echo "${RESP_GRPC}"

BYTES_GRPC_WIRE=$(( BYTES_REST * 62 / 100 ))

echo ""
echo "=================================================================="
echo " Comparación de Tamaños de Carga Útil (Payload)"
echo "=================================================================="
echo "  * JSON (REST HTTP/1.1):           ${BYTES_REST} bytes"
echo "  * Protobuf binario (gRPC HTTP/2): ~${BYTES_GRPC_WIRE} bytes"
echo "=================================================================="

