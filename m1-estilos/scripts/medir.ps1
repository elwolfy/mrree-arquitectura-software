# ==============================================================================
# medir.ps1 - Mide tiempos de arranque en logs y uso de memoria RAM en M1
# Uso: .\scripts\medir.ps1
# ==============================================================================

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$a = [char]0x00E1
$e = [char]0x00E9
$o = [char]0x00F3
$u = [char]0x00FA

$ErrorActionPreference = "SilentlyContinue"
docker info *>$null
$dockerExitCode = $LASTEXITCODE
$ErrorActionPreference = "Stop"

if ($dockerExitCode -ne 0) {
    Write-Host "Docker Desktop no est$($a) en ejecuci$($o)n: $($a)bralo y espere 'Engine running'" -ForegroundColor Red
    exit 1
}

$contenedoresActivos = docker ps --filter "name=^m1-" --format "{{.Names}}"
if ([string]::IsNullOrWhiteSpace($contenedoresActivos)) {
    Write-Host "[AVISO] No hay contenedores 'm1-*' en ejecuci$($o)n actualmente." -ForegroundColor Yellow
    exit 0
}

$listaNombres = @(($contenedoresActivos -split "`r?`n") | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })

Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host " 1. Tiempos de Arranque Registrados en Logs (M1)" -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan

foreach ($nombre in $listaNombres) {
    $ErrorActionPreference = "SilentlyContinue"
    $logs = docker logs $nombre 2>&1
    $ErrorActionPreference = "Stop"

    $lineaFunc = $logs | Select-String -Pattern "Funci.*n lista en (\d+) ms" | Select-Object -Last 1
    $lineaSpring = $logs | Select-String -Pattern "Started .* in [\d\.]+ seconds" | Select-Object -Last 1

    if ($lineaFunc) {
        $ms = $lineaFunc.Matches[0].Groups[1].Value
        Write-Host ("  * {0,-20}: Funci$($o)n lista en {1} ms" -f $nombre, $ms) -ForegroundColor Green
    } elseif ($lineaSpring) {
        Write-Host ("  * {0,-20}: {1}" -f $nombre, $lineaSpring.Matches[0].Value) -ForegroundColor Green
    } else {
        Write-Host ("  * {0,-20}: (servicio de infraestructura / sin m$($e)trica JVM)" -f $nombre) -ForegroundColor Gray
    }
}

Write-Host "`n==================================================================" -ForegroundColor Cyan
Write-Host " 2. Uso de Recursos en Tiempo Real (docker stats --no-stream)" -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan

docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}" $listaNombres

$lineasMem = docker stats --no-stream --format "{{.Name}}|{{.MemUsage}}" $listaNombres
$totalMiB = 0.0

foreach ($linea in ($lineasMem -split "`r?`n")) {
    if ([string]::IsNullOrWhiteSpace($linea)) { continue }
    $partes = $linea -split "\|"
    if ($partes.Count -lt 2) { continue }
    $usoTexto = ($partes[1] -split "/")[0].Trim()
    if ($usoTexto -match "^([\d\.]+)\s*GiB$") {
        $totalMiB += ([double]$Matches[1] * 1024.0)
    } elseif ($usoTexto -match "^([\d\.]+)\s*MiB$") {
        $totalMiB += [double]$Matches[1]
    } elseif ($usoTexto -match "^([\d\.]+)\s*KiB$") {
        $totalMiB += ([double]$Matches[1] / 1024.0)
    }
}

Write-Host "------------------------------------------------------------------" -ForegroundColor Cyan
Write-Host (" N$($u)mero de contenedores activos (m1-*): {0}" -f $listaNombres.Count) -ForegroundColor Yellow
Write-Host (" Memoria total consumida (aprox.):      {0:N2} MiB" -f $totalMiB) -ForegroundColor Yellow
Write-Host "==================================================================" -ForegroundColor Cyan
