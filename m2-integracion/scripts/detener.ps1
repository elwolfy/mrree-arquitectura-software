# ==============================================================================
# detener.ps1 - Detiene los contenedores del Modulo 2 y limpia los volumenes
# Uso: cd m2-integracion ; .\scripts\detener.ps1
# ==============================================================================

$ErrorActionPreference = "Stop"

$ModuloDir = Split-Path -Parent $PSScriptRoot
Push-Location $ModuloDir

try {
    Write-Host "[M2 - Integracion] Deteniendo contenedores y eliminando volumenes..." -ForegroundColor Yellow
    docker compose down -v --remove-orphans
    Write-Host "[OK] Entorno de M2 detenido y limpio." -ForegroundColor Green
}
finally {
    Pop-Location
}
