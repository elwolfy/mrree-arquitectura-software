# ==============================================================================
# detener.ps1 - Detiene todos los contenedores del Modulo 1 sin borrar datos
# Uso: .\scripts\detener.ps1
# ==============================================================================

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$a = [char]0x00E1
$o = [char]0x00F3

$ErrorActionPreference = "SilentlyContinue"
docker info *>$null
$dockerExitCode = $LASTEXITCODE
$ErrorActionPreference = "Stop"

if ($dockerExitCode -ne 0) {
    Write-Host "Docker Desktop no est$($a) en ejecuci$($o)n: $($a)bralo y espere 'Engine running'" -ForegroundColor Red
    exit 1
}

$ModuloDir = Split-Path -Parent $PSScriptRoot
Push-Location $ModuloDir

try {
    Write-Host "[M1 - Estilos] Deteniendo todos los contenedores activos (conservando datos)..." -ForegroundColor Yellow
    docker compose --profile "*" stop
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] 'docker compose --profile ""*"" stop' devolvi$($o) un error." -ForegroundColor Red
        exit 1
    }
    Write-Host "[OK] Contenedores del M$($o)dulo 1 detenidos." -ForegroundColor Green
}
finally {
    Pop-Location
}
