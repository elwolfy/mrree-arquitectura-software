# ==============================================================================
# levantar.ps1 - Construye, levanta y espera los healthchecks del Modulo 3 (Datos)
# Uso: cd m3-datos ; .\scripts\levantar.ps1
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

# Verificar si hay contenedores activos de M1 o M2 ocupando puertos (8080, 8081, 4200)
$ErrorActionPreference = "SilentlyContinue"
$contenedoresActivos = docker ps --format "{{.Names}}" 2>$null
$ErrorActionPreference = "Stop"

$conflictosM1M2 = @($contenedoresActivos | Where-Object { $_ -match "^(m1-|m2-)" })
if ($conflictosM1M2.Count -gt 0) {
    Write-Host "[ERROR] Los puertos 8080, 8081 o 4200 estan ocupados por contenedores activos de M1 o M2:" -ForegroundColor Red
    $conflictosM1M2 | ForEach-Object { Write-Host "        - $_" -ForegroundColor Yellow }
    Write-Host "        Detenga primero el modulo anterior (p. ej. '.\m1-estilos\scripts\detener.ps1' o '.\m2-integracion\scripts\detener.ps1') antes de levantar M3." -ForegroundColor Yellow
    exit 1
}

$ModuloDir = Split-Path -Parent $PSScriptRoot
Push-Location $ModuloDir

try {
    Write-Host "==================================================================" -ForegroundColor Cyan
    Write-Host " [M3 - Datos] Construyendo y levantando contenedores..." -ForegroundColor Cyan
    Write-Host "==================================================================" -ForegroundColor Cyan

    docker compose up -d --build --remove-orphans
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Fallo al construir o iniciar los contenedores con 'docker compose up'." -ForegroundColor Red
        exit 1
    }

    $Contenedores = @(
        @{ Nombre = "m3-postgres";       Servicio = "PostgreSQL 16 (Esquemas aislados)" },
        @{ Nombre = "m3-redis";          Servicio = "Redis 7 (Cache e Idempotencia 6379)" },
        @{ Nombre = "m3-rabbitmq";       Servicio = "RabbitMQ 3 Management" },
        @{ Nombre = "m3-mailpit";        Servicio = "Mailpit SMTP/Web" },
        @{ Nombre = "m3-pide";           Servicio = "Simulador PIDE WireMock (8089)" },
        @{ Nombre = "m3-citas";          Servicio = "citas-service (8081)" },
        @{ Nombre = "m3-tramites";       Servicio = "tramites-service (8082/9082 - JSONB)" },
        @{ Nombre = "m3-pagos";          Servicio = "pagos-service (8083)" },
        @{ Nombre = "m3-notificaciones"; Servicio = "notificaciones-service (8084)" },
        @{ Nombre = "m3-identidad";      Servicio = "identidad-service (8086)" },
        @{ Nombre = "m3-bff";            Servicio = "bff-web (8085)" },
        @{ Nombre = "m3-gateway";        Servicio = "API Gateway (8080)" },
        @{ Nombre = "m3-frontend";       Servicio = "Frontend Ciudadano M3 (4200)" }
    )

    $TiempoMaximoSegundos = 120
    $IntervaloSegundos = 2

    Write-Host ("`nEsperando a que los contenedores alcancen el estado 'healthy' (maximo {0}s por contenedor)..." -f $TiempoMaximoSegundos) -ForegroundColor Yellow

    foreach ($c in $Contenedores) {
        $nombre = $c.Nombre
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
                Write-Host ("  [OK] {0,-18} ({1}) -> healthy" -f $nombre, $c.Servicio) -ForegroundColor Green
                break
            }

            Start-Sleep -Seconds $IntervaloSegundos
            $transcurrido += $IntervaloSegundos
        }

        if ($estado -ne "healthy") {
            Write-Host ("[ERROR] Tiempo maximo de espera agotado ({0}s) para el contenedor '{1}' (ultimo estado: {2})." -f $TiempoMaximoSegundos, $nombre, $estado) -ForegroundColor Red
            exit 1
        }
    }

    # Garantizar ejecucion idempotente de 01-esquemas.sql si el volumen ya existia
    $ErrorActionPreference = "SilentlyContinue"
    docker exec m3-postgres psql -U postgres -d consular -q -f /docker-entrypoint-initdb.d/01-esquemas.sql *>$null
    $ErrorActionPreference = "Stop"

    Write-Host "`n==================================================================" -ForegroundColor Cyan
    Write-Host " Servicios activos - Modulo 3 (Gestion de Datos y Consistencia)" -ForegroundColor Cyan
    Write-Host "==================================================================" -ForegroundColor Cyan
    Write-Host "  * Frontend Ciudadano (Modo M3):      http://localhost:4200"
    Write-Host "  * API Gateway (Entrada unica):       http://localhost:8080"
    Write-Host "  * Swagger UI Agregado (Gateway):     http://localhost:8080/swagger-ui.html"
    Write-Host "  * bff-web (Swagger UI):              http://localhost:8085/swagger-ui.html"
    Write-Host "  * citas-service (Swagger UI):        http://localhost:8081/swagger-ui.html"
    Write-Host "  * tramites-service (Swagger UI):     http://localhost:8082/swagger-ui.html"
    Write-Host "  * tramites-service (gRPC):           localhost:9082"
    Write-Host "  * pagos-service (Swagger UI):        http://localhost:8083/swagger-ui.html"
    Write-Host "  * notificaciones-service (Swagger):  http://localhost:8084/swagger-ui.html"
    Write-Host "  * identidad-service (Swagger UI):    http://localhost:8086/swagger-ui.html"
    Write-Host "  * Simulador PIDE (WireMock):         http://localhost:8089/__admin/mappings"
    Write-Host "  * RabbitMQ Consola Web:              http://localhost:15672  (usuario: consular / clave: consular)"
    Write-Host "  * Mailpit Web UI:                    http://localhost:8025"
    Write-Host "  * Redis:                             localhost:6379"
    Write-Host "  * PostgreSQL:                        localhost:5432          (BD: consular)"
    Write-Host "==================================================================" -ForegroundColor Cyan
}
finally {
    Pop-Location
}

