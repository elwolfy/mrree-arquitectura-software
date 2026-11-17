# ==============================================================================
# limpiar.ps1 - Elimina contenedores, redes, volumenes y huerfanos del Modulo 1
# Uso: .\scripts\limpiar.ps1 [-Forzar]
# ==============================================================================

param(
    [switch]$Forzar
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$a = [char]0x00E1
$i = [char]0x00ED
$o = [char]0x00F3
$u = [char]0x00FA
$q = [char]0x00BF

$ErrorActionPreference = "SilentlyContinue"
docker info *>$null
$dockerExitCode = $LASTEXITCODE
$ErrorActionPreference = "Stop"

if ($dockerExitCode -ne 0) {
    Write-Host "Docker Desktop no est$($a) en ejecuci$($o)n: $($a)bralo y espere 'Engine running'" -ForegroundColor Red
    exit 1
}

if (-not $Forzar) {
    $respuesta = Read-Host "$($q)Est$($a) seguro de eliminar todos los contenedores y vol$($u)menes de datos de M1? (s/N)"
    if ($respuesta -notmatch "^(s|si|s$($i)|y|yes)$") {
        Write-Host "Operaci$($o)n cancelada por el usuario." -ForegroundColor Yellow
        exit 0
    }
}

$ModuloDir = Split-Path -Parent $PSScriptRoot
Push-Location $ModuloDir

try {
    Write-Host "[M1 - Estilos] Eliminando contenedores, redes y vol$($u)menes de datos..." -ForegroundColor Yellow
    docker compose --profile "*" down -v --remove-orphans
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] 'docker compose --profile ""*"" down -v --remove-orphans' devolvi$($o) un error." -ForegroundColor Red
        exit 1
    }
    Write-Host "[OK] Entorno del M$($o)dulo 1 limpiado por completo." -ForegroundColor Green
}
finally {
    Pop-Location
}
