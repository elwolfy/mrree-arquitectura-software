#!/usr/bin/env bash
# ==============================================================================
# analizar-calidad.sh — Levanta SonarQube (perfil calidad) y analiza tramites-service
# Uso: cd m4-observabilidad && ./scripts/analizar-calidad.sh
# ==============================================================================

set -euo pipefail

if ! docker info >/dev/null 2>&1; then
  echo "[ERROR] Docker Desktop no está en ejecución o el demonio no responde." >&2
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULO_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${MODULO_DIR}"

echo "=================================================================="
echo " [M4 - Calidad y Deuda Técnica] Iniciando m4-sonarqube (:9000)..."
echo " ADVERTENCIA: SonarQube consume ~2 GB de RAM (Demostración docente)"
echo "=================================================================="

docker compose --profile calidad up -d sonarqube

TIEMPO_MAXIMO_SEGUNDOS=240
INTERVALO_SEGUNDOS=5
transcurrido=0

echo "Esperando a que SonarQube alcance el estado UP en http://localhost:9000 (máximo ${TIEMPO_MAXIMO_SEGUNDOS}s)..."
while [ "$transcurrido" -lt "$TIEMPO_MAXIMO_SEGUNDOS" ]; do
  if curl -fsS http://localhost:9000/api/system/status 2>/dev/null | grep -q '"status":"UP"'; then
    echo "  [OK] m4-sonarqube -> status UP"
    break
  fi
  sleep "$INTERVALO_SEGUNDOS"
  transcurrido=$((transcurrido + INTERVALO_SEGUNDOS))
done

TOKEN_NAME="scanner-m4-$(date +%s)"
SONAR_TOKEN=$(curl -fsS -u admin:admin -X POST "http://localhost:9000/api/user_tokens/generate?name=${TOKEN_NAME}" 2>/dev/null | grep -oE '"token"\s*:\s*"[^"]+"' | cut -d'"' -f4 || true)

echo ""
echo "=================================================================="
echo " [M4 - Calidad] Ejecutando Maven SonarScanner sobre tramites-service"
echo "=================================================================="

if [ -n "${SONAR_TOKEN}" ]; then
  docker run --rm --network m4_frontal \
    -v "${MODULO_DIR}:/w" \
    -v "m4_maven_cache:/root/.m2" \
    -w /w/tramites-service \
    maven:3.9-eclipse-temurin-21 \
    mvn -B test-compile org.sonarsource.scanner.maven:sonar-maven-plugin:4.0.0.4121:sonar \
    -Dsonar.host.url=http://m4-sonarqube:9000 \
    -Dsonar.token="${SONAR_TOKEN}" \
    -Dsonar.projectKey=consular-tramites-service \
    -Dsonar.projectName="tramites-service (M4)"
else
  docker run --rm --network m4_frontal \
    -v "${MODULO_DIR}:/w" \
    -v "m4_maven_cache:/root/.m2" \
    -w /w/tramites-service \
    maven:3.9-eclipse-temurin-21 \
    mvn -B test-compile org.sonarsource.scanner.maven:sonar-maven-plugin:4.0.0.4121:sonar \
    -Dsonar.host.url=http://m4-sonarqube:9000 \
    -Dsonar.login=admin \
    -Dsonar.password=admin \
    -Dsonar.projectKey=consular-tramites-service \
    -Dsonar.projectName="tramites-service (M4)"
fi

echo ""
echo "=================================================================="
echo " [OK] Análisis de calidad publicado en SonarQube:"
echo "      http://localhost:9000/dashboard?id=consular-tramites-service"
echo "=================================================================="

