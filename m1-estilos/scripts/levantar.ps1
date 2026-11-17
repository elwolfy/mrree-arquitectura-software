# ==============================================================================
# levantar.ps1 - Construye, levanta y espera los healthchecks del Modulo 1
# Uso: .\scripts\levantar.ps1 [monolito|clean|microservicios|serverless]
# ==============================================================================

param(
    [Parameter(Position = 0)]
    [ValidateSet("monolito", "clean", "microservicios", "serverless")]
    [string]$Variante = "monolito"
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$a = [char]0x00E1
$e = [char]0x00E9
$i = [char]0x00ED
$o = [char]0x00F3
$u = [char]0x00FA

# a) Verificar con 'docker info' que Docker Desktop esta en ejecucion
$ErrorActionPreference = "SilentlyContinue"
docker info *>$null
$dockerExitCode = $LASTEXITCODE
$ErrorActionPreference = "Stop"

if ($dockerExitCode -ne 0) {
    Write-Host "Docker Desktop no est$($a) en ejecuci$($o)n: $($a)bralo y espere 'Engine running'" -ForegroundColor Red
    exit 1
}

# d) Detectar si el puerto 8081 o 4200 esta ocupado por otro modulo (ej. m2-citas)
foreach ($puerto in @(8081, 4200)) {
    $ErrorActionPreference = "SilentlyContinue"
    $ocupante = docker ps --filter "publish=$puerto" --format "{{.Names}}" 2>$null
    $ErrorActionPreference = "Stop"

    if (-not [string]::IsNullOrWhiteSpace($ocupante)) {
        $nombres = @(($ocupante -split "`r?`n") | Where-Object { $_ -and ($_ -notmatch "^m1-") })
        if ($nombres.Count -gt 0) {
            $nombreConflicto = ([string]$nombres[0]).Trim()
            Write-Host ("[ERROR] El puerto {0} est$($a) ocupado por el contenedor '{1}' de otro m$($o)dulo." -f $puerto, $nombreConflicto) -ForegroundColor Red
            Write-Host "        Det$($e)ngalo antes de continuar ejecutando:" -ForegroundColor Yellow
            Write-Host "        ..\m2-integracion\scripts\detener.ps1" -ForegroundColor Cyan
            exit 1
        }
    }
}

$ModuloDir = Split-Path -Parent $PSScriptRoot
Push-Location $ModuloDir

try {
    Write-Host "==================================================================" -ForegroundColor Cyan
    Write-Host (" [M1 - Estilos] Deteniendo variantes previas y levantando '{0}'..." -f $Variante) -ForegroundColor Cyan
    Write-Host "==================================================================" -ForegroundColor Cyan

    # Detener contenedores de otras variantes de M1 para liberar el puerto 8081
    $contenedoresVariantes = @(
        "m1-monolito",
        "m1-clean",
        "m1-citas",
        "m1-notificaciones",
        "m1-fn-consultas",
        "m1-fn-reservar",
        "m1-fn-notificar",
        "m1-enrutador"
    )
    $ErrorActionPreference = "SilentlyContinue"
    docker rm -f $contenedoresVariantes *>$null

    # Si m1-postgres ya esta corriendo de una variante anterior, reiniciar esquemas para evitar colisiones de Flyway entre variantes
    $pgActivo = docker ps --filter "name=^m1-postgres$" --format "{{.Names}}" 2>$null
    if (-not [string]::IsNullOrWhiteSpace($pgActivo)) {
        docker exec m1-postgres psql -U postgres -d consular -q -c "DROP SCHEMA IF EXISTS citas, tramites, notificaciones CASCADE;" *>$null
        docker exec m1-postgres psql -U postgres -d consular -q -f /docker-entrypoint-initdb.d/01-esquemas.sql *>$null
    }
    $ErrorActionPreference = "Stop"

    # b) Ejecutar docker compose up -d --build del perfil seleccionado
    docker compose --profile $Variante up -d --build
    if ($LASTEXITCODE -ne 0) {
        Write-Host ("[ERROR] 'docker compose --profile {0} up -d --build' devolvi$($o) un error. Revise la salida anterior." -f $Variante) -ForegroundColor Red
        exit 1
    }

    # Determinar lista de contenedores a esperar segun la variante
    $Contenedores = @(
        "m1-postgres",
        "m1-mailpit",
        "m1-frontend"
    )
    switch ($Variante) {
        "monolito"       { $Contenedores += @("m1-monolito") }
        "clean"          { $Contenedores += @("m1-clean") }
        "microservicios" { $Contenedores += @("m1-notificaciones", "m1-citas") }
        "serverless"     { $Contenedores += @("m1-fn-notificar", "m1-fn-consultas", "m1-fn-reservar", "m1-enrutador") }
    }

    # c) Esperar estado healthy con maximo 180 s
    $TiempoMaximoSegundos = 180
    $IntervaloSegundos = 2

    Write-Host ("`nEsperando a que los contenedores alcancen el estado 'healthy' (m$($a)ximo {0} s)..." -f $TiempoMaximoSegundos) -ForegroundColor Yellow

    foreach ($nombre in $Contenedores) {
        $transcurrido = 0
        $estado = "no-encontrado"

        while ($transcurrido -lt $TiempoMaximoSegundos) {
            $ErrorActionPreference = "SilentlyContinue"
            $salida = docker inspect --format="{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}" $nombre 2>$null
            $inspectExit = $LASTEXITCODE
            $ErrorActionPreference = "Stop"

            if ($inspectExit -eq 0 -and -not [string]::IsNullOrWhiteSpace($salida)) {
                $estado = $salida.Trim()
            } else {
                $estado = "no-encontrado"
            }

            if ($estado -eq "healthy") {
                Write-Host ("  [OK] {0,-20} -> healthy" -f $nombre) -ForegroundColor Green
                break
            }

            Start-Sleep -Seconds $IntervaloSegundos
            $transcurrido += $IntervaloSegundos
        }

        if ($estado -ne "healthy") {
            Write-Host ("[ERROR] El contenedor '{0}' no alcanz$($o) el estado 'healthy' en {1} s (estado actual: {2})." -f $nombre, $TiempoMaximoSegundos, $estado) -ForegroundColor Red
            Write-Host ("        Inspeccione su registro de eventos con: docker logs {0}" -f $nombre) -ForegroundColor Yellow
            exit 1
        }
    }

    # Dar margen a GeneradorDeCupos (ApplicationReadyEvent) para poblar cupos iniciales
    Start-Sleep -Seconds 2

    Write-Host "`n==================================================================" -ForegroundColor Cyan
    Write-Host (" Servicios activos - M$($o)dulo 1 (Variante: {0})" -f $Variante) -ForegroundColor Cyan
    Write-Host "==================================================================" -ForegroundColor Cyan
    Write-Host "  * Frontend Ciudadano: http://localhost:4200"
    Write-Host "  * API Principal:      http://localhost:8081"
    if ($Variante -eq "serverless") {
        Write-Host "  * Swagger UI:         No aplica en la variante serverless (funciones expuestas tras enrutador Nginx)"
    } else {
        Write-Host "  * Swagger UI:         http://localhost:8081/swagger-ui.html"
    }
    Write-Host "  * Mailpit Web:        http://localhost:8025"
    Write-Host "==================================================================" -ForegroundColor Cyan
}
finally {
    Pop-Location
}
