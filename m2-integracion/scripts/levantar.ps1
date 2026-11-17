# ==============================================================================
# levantar.ps1 - Construye, levanta y espera los healthchecks del Modulo 2
# Uso: cd m2-integracion ; .\scripts\levantar.ps1
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
    Write-Host "==================================================================" -ForegroundColor Cyan
    Write-Host " [M2 - Integracion] Construyendo y levantando contenedores..." -ForegroundColor Cyan
    Write-Host "==================================================================" -ForegroundColor Cyan

    docker compose up -d --build --remove-orphans
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Fallo al construir o iniciar los contenedores con 'docker compose up'." -ForegroundColor Red
        exit 1
    }

    $Contenedores = @(
        @{ Nombre = "m2-postgres";  Servicio = "PostgreSQL 16" },
        @{ Nombre = "m2-rabbitmq";  Servicio = "RabbitMQ 3 Management" },
        @{ Nombre = "m2-mailpit";   Servicio = "Mailpit SMTP/Web" },
        @{ Nombre = "m2-pide";      Servicio = "Simulador PIDE WireMock (8089)" },
        @{ Nombre = "m2-citas";     Servicio = "citas-service (8081)" },
        @{ Nombre = "m2-tramites";  Servicio = "tramites-service (8082/9082)" },
        @{ Nombre = "m2-identidad"; Servicio = "identidad-service (8086)" }
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
                Write-Host ("  [OK] {0,-15} ({1}) -> healthy" -f $nombre, $c.Servicio) -ForegroundColor Green
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

    Write-Host "`n==================================================================" -ForegroundColor Cyan
    Write-Host " Servicios activos - Modulo 2 (Integracion y Comunicacion)" -ForegroundColor Cyan
    Write-Host "==================================================================" -ForegroundColor Cyan
    Write-Host "  * citas-service (Swagger UI):     http://localhost:8081/swagger-ui.html"
    Write-Host "  * citas-service (OpenAPI):        http://localhost:8081/v3/api-docs"
    Write-Host "  * tramites-service (Swagger UI):  http://localhost:8082/swagger-ui.html"
    Write-Host "  * tramites-service (gRPC):        localhost:9082"
    Write-Host "  * identidad-service (Swagger UI): http://localhost:8086/swagger-ui.html"
    Write-Host "  * Simulador PIDE (WireMock):      http://localhost:8089/__admin/mappings"
    Write-Host "  * RabbitMQ Consola Web:           http://localhost:15672  (usuario: consular / clave: consular)"
    Write-Host "  * Mailpit Web UI:                 http://localhost:8025"
    Write-Host "  * PostgreSQL:                     localhost:5432          (BD: consular)"
    Write-Host "==================================================================" -ForegroundColor Cyan
}
finally {
    Pop-Location
}
