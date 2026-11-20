# ==============================================================================
# detener.ps1 - Detiene los contenedores del Modulo 4 (Observabilidad)
# Uso: cd m4-observabilidad ; .\scripts\detener.ps1
# ==============================================================================

$ErrorActionPreference = "Stop"

# Verificar con 'docker info' que Docker Desktop esta en ejecucion
$ErrorActionPreference = "SilentlyContinue"
docker info *>$null
$dockerExitCode = $LASTEXITCODE
$ErrorActionPreference = "Stop"

if ($dockerExitCode -ne 0) {
    Write-Host "[ERROR] Docker Desktop no esta en ejecucion o el demonio no responde." -ForegroundColor Red
    exit 1
}

$ModuloDir = Split-Path -Parent $PSScriptRoot
Push-Location $ModuloDir

try {
    Write-Host "[M4 - Observabilidad] Deteniendo contenedores..." -ForegroundColor Yellow
    docker --% compose --profile sidecar --profile c4 --profile calidad down --remove-orphans
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Fallo al detener los contenedores con 'docker compose down'." -ForegroundColor Red
        exit 1
    }
    Write-Host "[OK] Contenedores de M4 detenidos." -ForegroundColor Green
}
finally {
    Pop-Location
}
