# ==============================================================================
# levantar-c4.ps1 - Levanta solo el contenedor m4-structurizr (puerto 8090)
# Uso: cd m4-observabilidad ; .\scripts\levantar-c4.ps1
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
    Write-Host " [M4 - Modelo C4] Levantando Structurizr en http://localhost:8090..." -ForegroundColor Cyan
    Write-Host "==================================================================" -ForegroundColor Cyan

    docker compose --profile c4 up -d structurizr
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Fallo al iniciar el contenedor m4-structurizr." -ForegroundColor Red
        exit 1
    }

    $TiempoMaximoSegundos = 60
    $IntervaloSegundos = 2
    $transcurrido = 0
    $estado = "no-encontrado"

    while ($transcurrido -lt $TiempoMaximoSegundos) {
        $ErrorActionPreference = "SilentlyContinue"
        $salida = docker inspect --format="{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}" m4-structurizr 2>$null
        $inspectExit = $LASTEXITCODE
        $ErrorActionPreference = "Stop"

        if ($inspectExit -eq 0 -and -not [string]::IsNullOrWhiteSpace($salida)) {
            $estado = $salida.Trim()
        }

        if ($estado -eq "healthy") {
            Write-Host "  [OK] m4-structurizr (Modelo C4 en docs/c4/workspace.dsl) -> healthy" -ForegroundColor Green
            break
        }

        Start-Sleep -Seconds $IntervaloSegundos
        $transcurrido += $IntervaloSegundos
    }

    if ($estado -ne "healthy") {
        Write-Host ("[ERROR] Tiempo maximo agotado ({0}s) esperando a m4-structurizr (estado: {1})." -f $TiempoMaximoSegundos, $estado) -ForegroundColor Red
        exit 1
    }

    Write-Host "`n==================================================================" -ForegroundColor Cyan
    Write-Host " Structurizr activo: http://localhost:8090" -ForegroundColor Cyan
    Write-Host " Workspace DSL:      docs/c4/workspace.dsl" -ForegroundColor Cyan
    Write-Host "==================================================================" -ForegroundColor Cyan
}
finally {
    Pop-Location
}

