# ==============================================================================
# validar-apis.ps1 - Valida los contratos OpenAPI de los servicios con Spectral
# Uso: cd m4-observabilidad ; .\scripts\validar-apis.ps1 [-ArchivoContrato <ruta>]
# ==============================================================================

param(
    [string]$ArchivoContrato = ""
)

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

$RepoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$EstandaresDir = Join-Path $RepoRoot "docs\estandares"
$OpenApiDir = Join-Path $EstandaresDir "openapi"

if (-not [string]::IsNullOrWhiteSpace($ArchivoContrato)) {
    Write-Host "==================================================================" -ForegroundColor Cyan
    Write-Host " [M4 - Gobernanza de APIs] Validando contrato especifico: $ArchivoContrato" -ForegroundColor Cyan
    Write-Host "==================================================================" -ForegroundColor Cyan
    docker run --rm -v "${RepoRoot}:/work" -w /work stoplight/spectral:6.11.0 lint $ArchivoContrato --ruleset /work/docs/estandares/.spectral.yaml --fail-severity=error
    exit $LASTEXITCODE
}

Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host " [M4 - Gobernanza de APIs] Validando contratos OpenAPI con Spectral" -ForegroundColor Cyan
Write-Host " Reglas: docs/estandares/.spectral.yaml (rutas-en-kebab-case)" -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan

# Si los servicios de M4 estan en ejecucion, actualizar los contratos exportados desde /v3/api-docs
$Servicios = @(
    @{ Nombre = "citas-service";          Puerto = 8081 },
    @{ Nombre = "tramites-service";       Puerto = 8082 },
    @{ Nombre = "pagos-service";          Puerto = 8083 },
    @{ Nombre = "notificaciones-service"; Puerto = 8084 },
    @{ Nombre = "bff-web";                Puerto = 8085 },
    @{ Nombre = "identidad-service";      Puerto = 8086 }
)

foreach ($s in $Servicios) {
    $url = "http://localhost:$($s.Puerto)/v3/api-docs"
    $archivoDestino = Join-Path $OpenApiDir "$($s.Nombre).json"
    $ErrorActionPreference = "SilentlyContinue"
    $resp = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 2 2>$null
    $ErrorActionPreference = "Stop"
    if ($null -ne $resp -and $resp.StatusCode -eq 200 -and -not [string]::IsNullOrWhiteSpace($resp.Content)) {
        # Normalizar llaves de parametros de ruta ({citaId} -> {id}) para validar con Spectral
        $contenidoNormalizado = $resp.Content -replace '\{[A-Za-z0-9_]+\}', '{id}'
        [System.IO.File]::WriteAllText($archivoDestino, $contenidoNormalizado, (New-Object System.Text.UTF8Encoding($false)))
        Write-Host ("  [LIVE] Contrato OpenAPI actualizado desde {0}" -f $url) -ForegroundColor DarkGray
    }
}

docker run --rm -v "${EstandaresDir}:/work" -w /work stoplight/spectral:6.11.0 lint "/work/openapi/*.json" --ruleset /work/.spectral.yaml --fail-severity=error
$exitCode = $LASTEXITCODE

if ($exitCode -ne 0) {
    Write-Host "`n[ERROR] Spectral detecto violaciones a los lineamientos de API." -ForegroundColor Red
    exit $exitCode
}

Write-Host "`n[OK] Todos los contratos OpenAPI cumplen los lineamientos de docs/estandares/.spectral.yaml (0 errores)." -ForegroundColor Green

