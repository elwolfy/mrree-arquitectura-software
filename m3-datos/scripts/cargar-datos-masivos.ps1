# ==============================================================================
# cargar-datos-masivos.ps1 - Carga 1 000 000 de filas en tramites.vista_tramite
# Uso: cd m3-datos ; .\scripts\cargar-datos-masivos.ps1
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

$ErrorActionPreference = "SilentlyContinue"
$estado = docker inspect --format="{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}" m3-postgres 2>$null
$inspectExit = $LASTEXITCODE
$ErrorActionPreference = "Stop"

if ($inspectExit -ne 0 -or $estado.Trim() -ne "healthy") {
    Write-Host "[ERROR] El contenedor 'm3-postgres' no esta activo o saludable (estado: $estado)." -ForegroundColor Red
    Write-Host "        Ejecute primero '.\scripts\levantar.ps1'." -ForegroundColor Yellow
    exit 1
}

$RepoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$SqlFile = Join-Path $RepoRoot "docs\pruebas\sql\carga-masiva.sql"

if (-not (Test-Path $SqlFile)) {
    Write-Host "[ERROR] No se encontro el archivo SQL en: $SqlFile" -ForegroundColor Red
    exit 1
}

Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host " [M3 - Optimizacion] Cargando 1 000 000 de filas en vista_tramite..." -ForegroundColor Cyan
Write-Host " Archivo: $SqlFile" -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan

$cronometro = [System.Diagnostics.Stopwatch]::StartNew()
Get-Content -Raw $SqlFile | docker exec -i m3-postgres psql -U postgres -d consular -v ON_ERROR_STOP=1
$exitCode = $LASTEXITCODE
$cronometro.Stop()

if ($exitCode -ne 0) {
    Write-Host "[ERROR] Fallo la ejecucion de carga-masiva.sql." -ForegroundColor Red
    exit 1
}

Write-Host ("`n[OK] Carga masiva de 1 000 000 de filas completada en {0:N2} segundos." -f $cronometro.Elapsed.TotalSeconds) -ForegroundColor Green

