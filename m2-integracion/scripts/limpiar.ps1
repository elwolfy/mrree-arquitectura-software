# ==============================================================================
# limpiar.ps1 - Elimina contenedores, redes, volumenes y huerfanos del Modulo 2
# Uso: cd m2-integracion ; .\scripts\limpiar.ps1
# ==============================================================================

$ErrorActionPreference = "Stop"

# Verificar con 'docker info' que Docker Desktop esta en ejecucion
$ErrorActionPreference = "SilentlyContinue"
docker info *>$null
$dockerExitCode = $LASTEXITCODE
$ErrorActionPreference = "Stop"

if ($dockerExitCode -ne 0) {
    Write-Host "[ERROR] Docker Desktop no esta en ejecucion o el demonio no responde." -ForegroundColor Red
    Write-Host "        Inicie Docker Desktop, espere a que el motor este listo y vuelva a ejecutar este script." -ForegroundColor Yellow
    exit 1
}

$ModuloDir = Split-Path -Parent $PSScriptRoot
Push-Location $ModuloDir

try {
    Write-Host "[M2 - Integracion] Eliminando contenedores, redes, volumenes y huerfanos..." -ForegroundColor Yellow
    docker compose down -v --remove-orphans
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Fallo al ejecutar 'docker compose down -v --remove-orphans'." -ForegroundColor Red
        exit 1
    }
    Write-Host "[OK] Recursos de M2 eliminados completamente." -ForegroundColor Green
}
finally {
    Pop-Location
}

