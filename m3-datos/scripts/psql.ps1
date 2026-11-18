# ==============================================================================
# psql.ps1 - Abre una sesion psql dentro de m3-postgres como postgres sobre consular
# Uso interactivo: cd m3-datos ; .\scripts\psql.ps1
# Uso con comando: cd m3-datos ; .\scripts\psql.ps1 -c "\dn"
# ==============================================================================

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

$ErrorActionPreference = "SilentlyContinue"
$estado = docker inspect --format="{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}" m3-postgres 2>$null
$inspectExit = $LASTEXITCODE
$ErrorActionPreference = "Stop"

if ($inspectExit -ne 0 -or $estado.Trim() -ne "healthy") {
    Write-Host "[ERROR] El contenedor 'm3-postgres' no esta activo o saludable (estado: $estado)." -ForegroundColor Red
    Write-Host "        Ejecute primero '.\scripts\levantar.ps1'." -ForegroundColor Yellow
    exit 1
}

if ($args.Count -gt 0) {
    # En PowerShell 5.1, escapar comillas dobles internas al invocar ejecutables nativos
    $escapedArgs = foreach ($a in $args) {
        if ($a -is [string] -and $a.Contains('"') -and $PSVersionTable.PSVersion.Major -lt 7) {
            $a.Replace('"', '\"')
        } else {
            $a
        }
    }
    docker exec -i m3-postgres psql -U postgres -d consular @escapedArgs
} elseif ([Console]::IsInputRedirected) {
    $input | docker exec -i m3-postgres psql -U postgres -d consular
} else {
    docker exec -it m3-postgres psql -U postgres -d consular
}
exit $LASTEXITCODE
