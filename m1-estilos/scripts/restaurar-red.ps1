# ==============================================================================
# restaurar-red.ps1 - Reconecta la red m1_datos a m1-monolito o m1-clean
# Uso: .\scripts\restaurar-red.ps1
# ==============================================================================

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$a = [char]0x00E1
$e = [char]0x00E9
$o = [char]0x00F3

$ErrorActionPreference = "SilentlyContinue"
docker info *>$null
$dockerExitCode = $LASTEXITCODE
$ErrorActionPreference = "Stop"

if ($dockerExitCode -ne 0) {
    Write-Host "Docker Desktop no est$($a) en ejecuci$($o)n: $($a)bralo y espere 'Engine running'" -ForegroundColor Red
    exit 1
}

$contenedorObjetivo = $null
foreach ($candidato in @("m1-monolito", "m1-clean")) {
    $ErrorActionPreference = "SilentlyContinue"
    $enEjecucion = docker ps --filter "name=^$candidato$" --format "{{.Names}}" 2>$null
    $ErrorActionPreference = "Stop"
    if (-not [string]::IsNullOrWhiteSpace($enEjecucion)) {
        $contenedorObjetivo = $candidato
        break
    }
}

if (-not $contenedorObjetivo) {
    Write-Host "[ERROR] Ni 'm1-monolito' ni 'm1-clean' est$($a)n en ejecuci$($o)n actualmente." -ForegroundColor Red
    exit 1
}

Write-Host ("[M1 - CAP] Restaurando conexi$($o)n de red: conectando '{0}' a la red 'm1_datos'..." -f $contenedorObjetivo) -ForegroundColor Yellow
$ErrorActionPreference = "SilentlyContinue"
docker network connect m1_datos $contenedorObjetivo
$exitCode = $LASTEXITCODE
$ErrorActionPreference = "Stop"

if ($exitCode -ne 0) {
    Write-Host ("[AVISO] No se pudo conectar '{0}' a 'm1_datos' (puede que ya est$($e) conectado)." -f $contenedorObjetivo) -ForegroundColor Yellow
    exit 1
}

Write-Host ("[OK] '{0}' reconectado exitosamente a 'm1_datos'." -f $contenedorObjetivo) -ForegroundColor Green
