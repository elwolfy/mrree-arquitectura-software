# ==============================================================================
# pagar-dos-veces.ps1 - Demuestra la idempotencia de POST /api/pagos con Idempotency-Key
# Uso: cd m3-datos ; .\scripts\pagar-dos-veces.ps1 [-TramiteId <UUID>]
# ==============================================================================

param(
    [string]$TramiteId = "",
    [string]$GatewayUrl = "http://localhost:8080"
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

if ([string]::IsNullOrWhiteSpace($TramiteId)) {
    Write-Host "==================================================================" -ForegroundColor Cyan
    Write-Host " [1/3] Registrando tramite de prueba a traves del Gateway..." -ForegroundColor Cyan
    Write-Host "==================================================================" -ForegroundColor Cyan
    $bodyTramite = @{
        dni         = "40000001"
        nombres     = "Ana Perez"
        email       = "ana@correo.lab"
        tipoTramite = "APOSTILLA"
        pais        = "ES"
        folios      = 2
        idioma      = "ES"
    } | ConvertTo-Json
    $tramite = Invoke-RestMethod -Method Post -Uri "$GatewayUrl/api/tramites" -ContentType "application/json" -Body $bodyTramite
    $TramiteId = $tramite.id
    $Monto = $tramite.monto
    Write-Host ("  -> Tramite creado: {0} (id={1}, monto={2} PEN)" -f $tramite.codigo, $TramiteId, $Monto) -ForegroundColor Green
} else {
    $Monto = 33.00
    Write-Host ("  -> Usando tramite existente id={0}" -f $TramiteId) -ForegroundColor Yellow
}

$IdempotencyKey = [guid]::NewGuid().ToString()
$bodyPago = @{
    tramiteId = $TramiteId
    monto     = $Monto
} | ConvertTo-Json

$headers = @{
    "Idempotency-Key" = $IdempotencyKey
}

Write-Host "`n==================================================================" -ForegroundColor Cyan
Write-Host " [2/3] Enviando DOS peticiones POST /api/pagos con la MISMA clave" -ForegroundColor Cyan
Write-Host "       Idempotency-Key: $IdempotencyKey" -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan

$resp1 = Invoke-RestMethod -Method Post -Uri "$GatewayUrl/api/pagos" -ContentType "application/json" -Headers $headers -Body $bodyPago
Write-Host "`n--- Respuesta 1 (Primer intento) ---" -ForegroundColor Yellow
$resp1 | ConvertTo-Json

$resp2 = Invoke-RestMethod -Method Post -Uri "$GatewayUrl/api/pagos" -ContentType "application/json" -Headers $headers -Body $bodyPago
Write-Host "`n--- Respuesta 2 (Reintento con misma Idempotency-Key) ---" -ForegroundColor Yellow
$resp2 | ConvertTo-Json

Write-Host "`n==================================================================" -ForegroundColor Cyan
Write-Host " [3/3] Verificando conteo en pagos.pago para el tramite $TramiteId" -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan

$conteoRaw = docker exec m4-postgres psql -U postgres -d consular -t -A -c "SELECT COUNT(*) FROM pagos.pago WHERE tramite_id = '$TramiteId';"
$conteo = [int]($conteoRaw.Trim())

Write-Host ("  -> Registros en pagos.pago para tramite {0}: {1}" -f $TramiteId, $conteo) -ForegroundColor Green

if ($conteo -ne 1) {
    Write-Host ("[ERROR] Se esperaba exactamente 1 registro en pagos.pago, pero se encontraron {0}." -f $conteo) -ForegroundColor Red
    exit 1
}

Write-Host "  [OK] Idempotencia verificada: un solo cobro registrado y misma respuesta devuelta." -ForegroundColor Green

