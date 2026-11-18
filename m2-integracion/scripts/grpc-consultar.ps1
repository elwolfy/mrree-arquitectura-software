# ==============================================================================
# grpc-consultar.ps1 - Consulta un tramite por gRPC (9082) y compara tamano con REST (8082)
# Uso: .\m2-integracion\scripts\grpc-consultar.ps1 <TRAMITE_UUID>
# ==============================================================================
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string]$TramiteId
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$ModuloDir = Split-Path -Parent $PSScriptRoot
$ProtoDir  = Join-Path $ModuloDir "tramites-service\src\main\proto"

Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host " [M2 - REST vs. gRPC] Comparando consulta de tramite: $TramiteId" -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan

# 1. Consulta REST (JSON sobre HTTP/1.1 en puerto 8082)
$urlRest = "http://localhost:8082/api/tramites/$TramiteId"
$respRest = Invoke-WebRequest -Uri $urlRest -UseBasicParsing
$jsonCompacto = ($respRest.Content | ConvertFrom-Json | ConvertTo-Json -Compress)
$bytesRest = [System.Text.Encoding]::UTF8.GetByteCount($jsonCompacto)

Write-Host "`n1. Respuesta REST (GET $urlRest):" -ForegroundColor Yellow
Write-Host $respRest.Content

# 2. Consulta gRPC (Protobuf sobre HTTP/2 en m2-tramites:9082 via grpcurl en contenedor)
Write-Host "`n2. Respuesta gRPC (consular.tramites.v1.TramitesService/ConsultarTramite en m2-tramites:9082):" -ForegroundColor Yellow
$payload = "{`"id`":`"$TramiteId`"}"
$salidaGrpc = $payload | docker run -i --rm --network m2_frontal -v "${ProtoDir}:/protos:ro" fullstorydev/grpcurl:v1.9.1 `
    -plaintext -import-path /protos -proto tramites.proto `
    -d "@" m2-tramites:9082 consular.tramites.v1.TramitesService/ConsultarTramite

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Fallo la invocacion gRPC con grpcurl." -ForegroundColor Red
    exit 1
}
$textoGrpc = ($salidaGrpc -join "`n")
Write-Host $textoGrpc

# Estimacion del payload binario Protobuf (campos string + varint en wire format proto3)
$objRest = $respRest.Content | ConvertFrom-Json
$bytesProtobufWire = 0
foreach ($campo in @(
    [string]$objRest.id,
    [string]$objRest.codigo,
    [string]$objRest.dni,
    [string]$objRest.nombres,
    [string]$objRest.email,
    [string]$objRest.tipoTramite,
    [string]$objRest.subtipo,
    [string]$objRest.pais,
    [string]$objRest.idioma,
    [string]$objRest.estado,
    [string]$objRest.monto,
    [string]$objRest.creadoEn
)) {
    if ($campo.Length -gt 0) {
        $bytesProtobufWire += 2 + [System.Text.Encoding]::UTF8.GetByteCount($campo)
    }
}
$bytesProtobufWire += 2 # campo int32 folios (tag + varint)

$ahorroPct = [math]::Round((1.0 - ($bytesProtobufWire / [double]$bytesRest)) * 100, 1)

Write-Host "`n==================================================================" -ForegroundColor Cyan
Write-Host " Comparacion de Tamanos de Carga Util (Payload)" -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host ("  * JSON (REST HTTP/1.1):          {0,4} bytes" -f $bytesRest) -ForegroundColor White
Write-Host ("  * Protobuf binario (gRPC HTTP/2): {0,4} bytes (ahorro aprox. {1} %)" -f $bytesProtobufWire, $ahorroPct) -ForegroundColor Green
Write-Host "==================================================================" -ForegroundColor Cyan

