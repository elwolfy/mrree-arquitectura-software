# ==============================================================================
# simular-particion.ps1 - Desconecta la red m1_datos de m1-monolito o m1-clean
# Uso: .\scripts\simular-particion.ps1
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
    Write-Host "        Levante primero una de esas variantes con: .\scripts\levantar.ps1 monolito" -ForegroundColor Yellow
    exit 1
}

Write-Host ("[M1 - CAP] Simulando partici$($o)n de red: desconectando '{0}' de la red 'm1_datos'..." -f $contenedorObjetivo) -ForegroundColor Yellow
$ErrorActionPreference = "SilentlyContinue"
docker network disconnect m1_datos $contenedorObjetivo
$exitCode = $LASTEXITCODE
$ErrorActionPreference = "Stop"

if ($exitCode -ne 0) {
    Write-Host ("[AVISO] No se pudo desconectar '{0}' de 'm1_datos' (puede que ya est$($e) desconectado)." -f $contenedorObjetivo) -ForegroundColor Yellow
    exit 1
}

Write-Host ("[OK] '{0}' desconectado de 'm1_datos' (PostgreSQL inaccesible)." -f $contenedorObjetivo) -ForegroundColor Green
Write-Host "     Observe en http://localhost:4200 o en /api/oficinas/MAD/disponibilidad el comportamiento CP vs AP." -ForegroundColor Cyan
Write-Host "     Para restaurar la conexi$($o)n ejecute: .\scripts\restaurar-red.ps1" -ForegroundColor Cyan
