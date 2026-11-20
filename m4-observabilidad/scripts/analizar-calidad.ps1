# ==============================================================================
# analizar-calidad.ps1 - Levanta SonarQube (perfil calidad) y analiza tramites-service
# Uso: cd m4-observabilidad ; .\scripts\analizar-calidad.ps1
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

$ModuloDir = Split-Path -Parent $PSScriptRoot
Push-Location $ModuloDir

try {
    Write-Host "==================================================================" -ForegroundColor Cyan
    Write-Host " [M4 - Calidad y Deuda Tecnica] Iniciando m4-sonarqube (:9000)..." -ForegroundColor Cyan
    Write-Host " ADVERTENCIA: SonarQube consume ~2 GB de RAM (Demostracion docente)" -ForegroundColor Yellow
    Write-Host "==================================================================" -ForegroundColor Cyan

    docker compose --profile calidad up -d sonarqube
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Fallo al iniciar el contenedor m4-sonarqube." -ForegroundColor Red
        exit 1
    }

    $TiempoMaximoSegundos = 240
    $IntervaloSegundos = 5
    $transcurrido = 0
    $listo = $false

    Write-Host ("Esperando a que SonarQube alcance el estado UP en http://localhost:9000 (maximo {0}s)..." -f $TiempoMaximoSegundos) -ForegroundColor Yellow

    while ($transcurrido -lt $TiempoMaximoSegundos) {
        $statusResp = $null
        try {
            $statusResp = Invoke-RestMethod -Uri "http://localhost:9000/api/system/status" -TimeoutSec 3 -ErrorAction Stop
        } catch {
            $statusResp = $null
        }

        if ($null -ne $statusResp -and $statusResp.status -eq "UP") {
            Write-Host "  [OK] m4-sonarqube -> status UP" -ForegroundColor Green
            $listo = $true
            break
        }

        Start-Sleep -Seconds $IntervaloSegundos
        $transcurrido += $IntervaloSegundos
    }

    if (-not $listo) {
        Write-Host "[ERROR] Tiempo maximo agotado esperando a que m4-sonarqube alcance status UP." -ForegroundColor Red
        exit 1
    }

    # Generar un token de analisis mediante la API REST de SonarQube (admin/admin o contraseña de laboratorio)
    # En SonarQube 10+/26+, la primera vez puede requerir cambiar la contraseña inicial de admin:admin
    $basicAdmin = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("admin:admin"))
    $hdrInit = @{ Authorization = "Basic $basicAdmin" }
    try {
        Invoke-RestMethod -Method Post -Uri "http://localhost:9000/api/users/change_password?login=admin&previousPassword=admin&password=Consular2026!" -Headers $hdrInit -TimeoutSec 5 -ErrorAction Stop *>$null
    } catch {
        # Ignorar si la contraseña ya habia sido cambiada o si acepta admin
    }

    $Credenciales = @("admin:Consular2026!", "admin:admin")
    $AuthHeader = $null

    foreach ($cred in $Credenciales) {
        $encoded = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes($cred))
        $hdr = @{ Authorization = "Basic $encoded" }
        try {
            $val = Invoke-RestMethod -Uri "http://localhost:9000/api/authentication/validate" -Headers $hdr -TimeoutSec 5 -ErrorAction Stop
            if ($null -ne $val -and $val.valid -eq $true) {
                $AuthHeader = $hdr
                break
            }
        } catch {
            # Probar siguiente credencial
        }
    }

    $TokenName = "scanner-m4-" + [guid]::NewGuid().ToString().Substring(0, 8)
    $SonarToken = ""
    try {
        $tokenResp = Invoke-RestMethod -Method Post -Uri "http://localhost:9000/api/user_tokens/generate?name=$TokenName" -Headers $AuthHeader -TimeoutSec 5 -ErrorAction Stop
        if ($null -ne $tokenResp -and -not [string]::IsNullOrWhiteSpace($tokenResp.token)) {
            $SonarToken = $tokenResp.token
        }
    } catch {
        $SonarToken = ""
    }

    Write-Host "`n==================================================================" -ForegroundColor Cyan
    Write-Host " [M4 - Calidad] Ejecutando Maven SonarScanner sobre tramites-service" -ForegroundColor Cyan
    Write-Host "==================================================================" -ForegroundColor Cyan

    if (-not [string]::IsNullOrWhiteSpace($SonarToken)) {
        docker run --rm --network m4_frontal `
            -v "${ModuloDir}:/w" `
            -v "m4_maven_cache:/root/.m2" `
            -w /w/tramites-service `
            maven:3.9-eclipse-temurin-21 `
            mvn -B test-compile org.sonarsource.scanner.maven:sonar-maven-plugin:4.0.0.4121:sonar `
            "-Dsonar.host.url=http://m4-sonarqube:9000" `
            "-Dsonar.token=$SonarToken" `
            "-Dsonar.projectKey=consular-tramites-service" `
            "-Dsonar.projectName=tramites-service (M4)"
    } else {
        docker run --rm --network m4_frontal `
            -v "${ModuloDir}:/w" `
            -v "m4_maven_cache:/root/.m2" `
            -w /w/tramites-service `
            maven:3.9-eclipse-temurin-21 `
            mvn -B test-compile org.sonarsource.scanner.maven:sonar-maven-plugin:4.0.0.4121:sonar `
            "-Dsonar.host.url=http://m4-sonarqube:9000" `
            "-Dsonar.login=admin" `
            "-Dsonar.password=admin" `
            "-Dsonar.projectKey=consular-tramites-service" `
            "-Dsonar.projectName=tramites-service (M4)"
    }

    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Fallo el analisis de SonarQube sobre tramites-service." -ForegroundColor Red
        exit 1
    }

    Write-Host "`n==================================================================" -ForegroundColor Cyan
    Write-Host " [OK] Analisis de calidad publicado en SonarQube:" -ForegroundColor Green
    Write-Host "      http://localhost:9000/dashboard?id=consular-tramites-service" -ForegroundColor Cyan
    Write-Host "      (Usuario: admin / Clave inicial: admin)" -ForegroundColor Cyan
    Write-Host "==================================================================" -ForegroundColor Cyan
}
finally {
    Pop-Location
}
