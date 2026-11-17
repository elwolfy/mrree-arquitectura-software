# ==============================================================================
# carga.ps1 - Ejecuta pruebas de carga k6 sobre la variante activa de M1 (:8081)
# Uso: .\scripts\carga.ps1 [citas|pico]
# ==============================================================================

param(
    [Parameter(Position = 0)]
    [ValidateSet("citas", "pico")]
    [string]$Escenario = "citas"
)

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
    $archivoScript = if ($Escenario -eq "pico") { "citas-pico.js" } else { "citas.js" }
    $rutaLocal = Join-Path $ModuloDir "..\docs\pruebas\k6\$archivoScript"

    if (-not (Test-Path $rutaLocal)) {
        Write-Host ("[ERROR] No se encontr$($o) el script k6 en '{0}'." -f $rutaLocal) -ForegroundColor Red
        exit 1
    }

    $env:K6_SCRIPT = $archivoScript
    if ([string]::IsNullOrWhiteSpace($env:OBJETIVO)) {
        $env:OBJETIVO = "http://host.docker.internal:8081"
    }

    Write-Host "==================================================================" -ForegroundColor Cyan
    Write-Host (" [M1 - Carga k6] Ejecutando '{0}' contra {1}..." -f $env:K6_SCRIPT, $env:OBJETIVO) -ForegroundColor Cyan
    Write-Host "==================================================================" -ForegroundColor Cyan

    docker compose --profile carga run --rm k6
    if ($LASTEXITCODE -ne 0) {
        Write-Host ("[ERROR] La ejecuci$($o)n de k6 ('{0}') finaliz$($o) con errores." -f $archivoScript) -ForegroundColor Red
        exit 1
    }
}
finally {
    Remove-Item Env:K6_SCRIPT -ErrorAction SilentlyContinue
    Pop-Location
}
