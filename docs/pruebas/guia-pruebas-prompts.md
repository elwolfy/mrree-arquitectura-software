# Guía Maestra de Pruebas por Prompt (`mrree-arquitectura-software`)

> **Propósito:** Este documento centraliza los comandos y pasos de verificación de **todas** las aplicaciones y módulos generados por cada prompt del repositorio. Se actualiza incrementalmente cada vez que se completa un nuevo prompt.
>
> **Convención de directorio:** Todos los comandos de esta guía asumen que tu terminal PowerShell está ubicada en la carpeta raíz del repositorio:
> ```powershell
> cd c:\Jorge-Projects\mre_arquitectura\mrree-arquitectura-software
> ```

---

## Estado de avance por Prompt

| Prompt | Descripción | Carpeta / Artefacto | Estado |
| --- | --- | --- | --- |
| **01** | Bootstrap del repositorio, reglas y contrato global | Raíz, `.agents/`, `docs/especificacion/contrato-global.md` | Completado |
| **02** | M1: Monolito modular con arquitectura hexagonal | `m1-estilos/monolito-modular` | Completado |
| **03** | M1: Comparativa Clean Architecture vs. Hexagonal | `m1-estilos/comparativa-clean`, `docs/matrices/m1-hexagonal-vs-clean.md` | Completado |
| **04** | M1: Variante Microservicios (`citas` + `notificaciones`) | `m1-estilos/microservicios` | Completado |
| **05** | M1: Variante Serverless (Spring Cloud Function + Nginx) | `m1-estilos/serverless` | Completado |
| **06** | Frontend Ciudadano (Angular 19 + PrimeNG 19) | `frontend/` (`consular-frontend`) | Completado |
| **07** | M1: Compose unificado, scripts k6 y matrices de estilos | `m1-estilos/docker-compose.yml`, `docs/pruebas/k6` | Pendiente |
| **08-1** | M2: Esqueleto base, RabbitMQ y `citas-service` | `m2-integracion/citas-service` | Completado |
| **08-2 a 08-7** | M2: `tramites`, `identidad`, `notificaciones`, `pagos`, `bff`, `gateway`, `sidecar` | `m2-integracion/*` | Pendiente |
| **09** | M3: Persistencia, Saga, Outbox, Event Sourcing y CQRS | `m3-datos/*` | Pendiente |
| **10** | M4: Observabilidad (OpenTelemetry + Grafana LGTM) | `m4-observabilidad/*` | Pendiente |
| **11–13** | ADRs, C4, soluciones de asignaciones y verificación final | `docs/*` | Pendiente |

---

## Prompt 01 — Bootstrap del Repositorio

### Qué verifica
Que la estructura base del monorepo, las reglas (`.agents/rules/01..06`), el contrato global (`docs/especificacion/contrato-global.md`) y el ADR-000 estén presentes.

```powershell
Get-ChildItem -Path .agents/rules
Get-Content docs/adr/ADR-000-version-de-plataforma.md -TotalCount 25
```

---

## Prompt 02 — M1: Monolito Modular Hexagonal (`m1-estilos/monolito-modular`)

### 1. Pruebas automatizadas (Unitarias + ArchUnit + Spring Modulith + Testcontainers)
```powershell
mvn -B -f m1-estilos verify -pl monolito-modular
```

### 2. Pruebas en contenedores Docker (`:8081`)
```powershell
# Levantar PostgreSQL, Mailpit y el Monolito
docker compose -f m1-estilos/docker-compose.dev.yml --profile monolito up -d --build --wait

# 2.1 Información del sistema (variante: MONOLITO_MODULAR, arquitecturaInterna: HEXAGONAL)
Invoke-RestMethod -Uri "http://localhost:8081/api/sistema/info" | ConvertTo-Json

# 2.2 Listar las 6 oficinas consulares semilla
Invoke-RestMethod -Uri "http://localhost:8081/api/oficinas" | Format-Table id, ciudad, pais, zonaHoraria

# 2.3 Consultar disponibilidad de Madrid (genera los cupos del día hábil si no existen)
Invoke-RestMethod -Uri "http://localhost:8081/api/oficinas/MAD/disponibilidad?fecha=2026-11-18" | ConvertTo-Json -Depth 4

# 2.4 Calcular tarifa con patrón Strategy (LEGALIZACION y APOSTILLA)
Invoke-RestMethod -Uri "http://localhost:8081/api/tarifas?tipoTramite=APOSTILLA&pais=ES" | ConvertTo-Json

# 2.5 Reservar una cita (201 Created, notificacion: ENVIADA)
$body = [System.Text.Encoding]::UTF8.GetBytes('{"dni":"40000001","nombres":"Ana Perez","email":"ana@correo.lab","oficinaId":"MAD","fecha":"2026-11-18","hora":"09:00","tipoTramite":"LEGALIZACION"}')
$cita = Invoke-RestMethod -Uri "http://localhost:8081/api/citas" -Method POST -ContentType "application/json; charset=utf-8" -Body $body
$cita | ConvertTo-Json

# 2.6 Consultar cita por ID y por DNI
Invoke-RestMethod -Uri "http://localhost:8081/api/citas/$($cita.id)" | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8081/api/citas?dni=40000001" | ConvertTo-Json

# 2.7 Cancelar la cita (204 No Content)
Invoke-WebRequest -Uri "http://localhost:8081/api/citas/$($cita.id)" -Method DELETE -UseBasicParsing | Select-Object StatusCode
```

### 3. Experimento didáctico CAP (Modo `AP` vs `CP` al caer PostgreSQL)
```powershell
# Reiniciar el monolito en modo AP
$env:CONSISTENCIA_MODO = "AP"
docker compose -f m1-estilos/docker-compose.dev.yml --profile monolito up -d --wait

# Calentar la caché consultando disponibilidad con PostgreSQL activo (origen: BASE_DE_DATOS)
Invoke-RestMethod -Uri "http://localhost:8081/api/oficinas/MAD/disponibilidad?fecha=2026-11-18" | Select-Object oficinaId, origen, actualizadoHaceSegundos

# Detener PostgreSQL y volver a consultar disponibilidad -> responde 200 desde CACHE
Start-Sleep -Seconds 2
docker stop m1-postgres
Invoke-RestMethod -Uri "http://localhost:8081/api/oficinas/MAD/disponibilidad?fecha=2026-11-18" | Select-Object oficinaId, origen, actualizadoHaceSegundos
```

### 4. Limpieza del módulo
```powershell
docker compose -f m1-estilos/docker-compose.dev.yml --profile monolito down -v --remove-orphans
Remove-Item Env:CONSISTENCIA_MODO -ErrorAction SilentlyContinue
```

---

## Prompt 03 — M1: Comparativa Clean Architecture (`m1-estilos/comparativa-clean`)

### 1. Pruebas automatizadas (Use Cases, Presenter, ArchUnit y Testcontainers)
```powershell
mvn -B -f m1-estilos verify -pl comparativa-clean
```

### 2. Ejecución local y consulta de endpoints (`:8081`)
```powershell
# Levantar solo PostgreSQL y Mailpit de apoyo
docker compose -f m1-estilos/docker-compose.dev.yml up -d postgres mailpit --wait

# Ejecutar la aplicación Clean Architecture (en otra ventana o en segundo plano)
mvn -f m1-estilos spring-boot:run -pl comparativa-clean
```
En otra terminal PowerShell:
```powershell
# Verificar que arquitecturaInterna reporta CLEAN
Invoke-RestMethod -Uri "http://localhost:8081/api/sistema/info" | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8081/api/oficinas" | Format-Table id, nombre, pais
```
Para detener PostgreSQL y Mailpit al finalizar:
```powershell
docker compose -f m1-estilos/docker-compose.dev.yml down -v --remove-orphans
```

---

## Prompt 04 — M1: Variante Microservicios (`m1-estilos/microservicios`)

### 1. Pruebas automatizadas (Unitarias + WireMock Circuit Breaker + Testcontainers)
```powershell
mvn -B -f m1-estilos verify -pl microservicios/citas-service,microservicios/notificaciones-service
```

### 2. Pruebas en contenedores Docker (`--profile microservicios`)
```powershell
# Levantar postgres, mailpit, m1-notificaciones (:8084) y m1-citas (:8081)
docker compose -f m1-estilos/docker-compose.dev.yml --profile microservicios up -d --build --wait

# 2.1 Verificar info del sistema (variante: MICROSERVICIOS)
Invoke-RestMethod -Uri "http://localhost:8081/api/sistema/info" | ConvertTo-Json

# 2.2 Reservar cita con m1-notificaciones activo -> responde 201 con notificacion: ENVIADA
$body1 = [System.Text.Encoding]::UTF8.GetBytes('{"dni":"40000010","nombres":"Luis Ramos","email":"luis@correo.lab","oficinaId":"MAD","fecha":"2026-11-18","hora":"09:30","tipoTramite":"PASAPORTE"}')
Invoke-RestMethod -Uri "http://localhost:8081/api/citas" -Method POST -ContentType "application/json; charset=utf-8" -Body $body1 | ConvertTo-Json

# 2.3 Prueba de resiliencia (Circuit Breaker + Timeout 2 s): detener m1-notificaciones
docker stop m1-notificaciones

# Reservar otra cita -> NO falla (a diferencia del monolito); responde 201 en < 3 s con notificacion: PENDIENTE
$body2 = [System.Text.Encoding]::UTF8.GetBytes('{"dni":"40000011","nombres":"Maria Diaz","email":"maria@correo.lab","oficinaId":"MAD","fecha":"2026-11-18","hora":"10:00","tipoTramite":"PASAPORTE"}')
$sw = [System.Diagnostics.Stopwatch]::StartNew()
$citaDeg = Invoke-RestMethod -Uri "http://localhost:8081/api/citas" -Method POST -ContentType "application/json; charset=utf-8" -Body $body2
$sw.Stop()
Write-Host "Tiempo de respuesta: $($sw.ElapsedMilliseconds) ms | Estado notificacion: $($citaDeg.notificacion)"
$citaDeg | ConvertTo-Json
```

### 3. Limpieza del módulo
```powershell
docker compose -f m1-estilos/docker-compose.dev.yml --profile microservicios down -v --remove-orphans
```

---

## Prompt 05 — M1: Variante Serverless (`m1-estilos/serverless`)

### 1. Pruebas automatizadas (Funciones unitarias sin Spring + Testcontainers)
```powershell
mvn -B -f m1-estilos verify -pl serverless/funciones-citas
```

### 2. Pruebas en contenedores Docker (`--profile serverless`)
```powershell
# Levantar postgres, mailpit, 3 contenedores de funciones y m1-enrutador (:8081)
docker compose -f m1-estilos/docker-compose.dev.yml --profile serverless up -d --build --wait

# 2.1 Verificar logs de arranque en frío ("Función lista en N ms")
docker logs m1-fn-consultas | Select-String "Función lista en"
docker logs m1-fn-reservar  | Select-String "Función lista en"
docker logs m1-fn-notificar | Select-String "Función lista en"

# 2.2 Verificar cabecera X-Funcion-Invocada e infoSistema (variante: SERVERLESS)
$respInfo = Invoke-WebRequest -Uri "http://localhost:8081/api/sistema/info" -UseBasicParsing
Write-Host "X-Funcion-Invocada: $($respInfo.Headers['X-Funcion-Invocada']) | Body: $($respInfo.Content)"

# 2.3 Reservar cita con m1-fn-notificar activo (< 300 ms -> notificacion: ENVIADA)
$body1 = [System.Text.Encoding]::UTF8.GetBytes('{"dni":"45678920","nombres":"Lucia Paredes","email":"lucia@correo.lab","oficinaId":"MAD","tipoTramite":"PASAPORTE","fecha":"2026-11-18","hora":"11:30"}')
$respCita1 = Invoke-WebRequest -Uri "http://localhost:8081/api/citas" -Method POST -ContentType "application/json; charset=utf-8" -Body $body1 -UseBasicParsing
Write-Host "Status: $($respCita1.StatusCode) | X-Funcion-Invocada: $($respCita1.Headers['X-Funcion-Invocada']) | Body: $($respCita1.Content)"

# 2.4 Detener m1-fn-notificar -> la reserva responde 201 en ~300 ms con notificacion: PENDIENTE
docker stop m1-fn-notificar
$body2 = [System.Text.Encoding]::UTF8.GetBytes('{"dni":"45678921","nombres":"Carlos Mendoza","email":"carlos@correo.lab","oficinaId":"MAD","tipoTramite":"PASAPORTE","fecha":"2026-11-18","hora":"12:00"}')
$respCita2 = Invoke-WebRequest -Uri "http://localhost:8081/api/citas" -Method POST -ContentType "application/json; charset=utf-8" -Body $body2 -UseBasicParsing
Write-Host "Status: $($respCita2.StatusCode) | Body: $($respCita2.Content)"
```

### 3. Limpieza del módulo
```powershell
docker compose -f m1-estilos/docker-compose.dev.yml --profile serverless down -v --remove-orphans
```

---

## Prompt 06 — Frontend Ciudadano Angular + PrimeNG (`frontend/`)

### 1. Linter, pruebas unitarias (`ChromeHeadless`) e imagen Docker
```powershell
Set-Location frontend
npm run lint
npm test
Set-Location ..
docker build -t consular-frontend frontend/
```

### 2. Prueba interactiva en el navegador (`http://localhost:4200`) con el Monolito (`:8081`)
```powershell
# Levantar el monolito en modo AP (para poder probar tanto reserva como caché CAP)
$env:CONSISTENCIA_MODO = "AP"
docker compose -f m1-estilos/docker-compose.dev.yml --profile monolito up -d --wait

# Levantar el contenedor del frontend en :4200 configurado para M1
docker rm -f m1-frontend 2>$null
docker run -d --name m1-frontend -p 4200:80 `
  -e API_BASE_URL=http://localhost:8081 `
  -e MODULO=m1 `
  -e FUNC_CITAS=true `
  -e FUNC_TRAMITES=false `
  -e FUNC_PAGOS=false `
  -e FUNC_BFF=false `
  -e FUNC_HISTORIAL=false `
  consular-frontend
```

#### Lista de verificación visual en `http://localhost:4200`:
1. **Menú y Pie de Página (`MODULO=m1`)**:
   - En la barra superior solo aparecen **Inicio**, **Reservar Cita** y **Consultar Citas** (no aparecen las rutas de trámites ni *Mi Espacio*).
   - En la tarjeta **"Sistema en ejecución"** se observa `MONOLITO_MODULAR`, `HEXAGONAL` y `AP`.
   - El pie de página muestra `Módulo activo: m1 | API Base URL: http://localhost:8081`.
2. **Reserva paso a paso (`/citas/nueva`)**:
   - **Paso 1 (Oficina)**: Seleccionar **Madrid (MAD)** → *Siguiente*.
   - **Paso 2 (Fecha)**: Elegir un día hábil entre mañana y +30 días → **Consultar disponibilidad**.
   - **Paso 3 (Cupo)**: Verificar la etiqueta verde **"Base de datos"**, elegir un horario libre (ej. `08:00`) → *Siguiente*.
   - **Paso 4 (Datos)**: Completar DNI (`40000001`), nombre, email y trámite → **Confirmar Reserva**.
   - **Paso 5 (Confirmación)**: Verificar que se muestra el código (`CIT-...`), estado `RESERVADA` y **Estado de Notificación: `ENVIADA`**.
3. **Banner de error `ProblemDetail` (`409 Conflict`)**:
   - Pulsar **Reservar otra cita**, elegir la misma oficina (`MAD`), la misma fecha, otro horario y el mismo DNI (`40000001`).
   - Confirmar que aparece el mensaje de error superior (`p-message`) mostrando el `title` (**Cita duplicada**) y el `detail` exacto del `ProblemDetail`.
4. **Etiqueta CAP en modo `AP` al caer la base de datos**:
   - Ejecutar en terminal:
     ```powershell
     docker stop m1-postgres
     ```
   - En el **Paso 3 (Cupo)** de `/citas/nueva`, pulsar el botón de refrescar disponibilidad: la etiqueta cambia a amarillo mostrando **"Caché – actualizado hace N s"**.

### 3. Limpieza del entorno
```powershell
docker rm -f m1-frontend
docker compose -f m1-estilos/docker-compose.dev.yml --profile monolito down -v --remove-orphans
Remove-Item Env:CONSISTENCIA_MODO -ErrorAction SilentlyContinue
```

---

## Prompt 08-1 — M2: Base de Integración y `citas-service` con RabbitMQ (`m2-integracion/`)

### 1. Pruebas automatizadas (Unitarias + ArchUnit + Testcontainers PostgreSQL y RabbitMQ)
```powershell
mvn -B -f m2-integracion verify
```

### 2. Pruebas en contenedores Docker (`m2-integracion/docker-compose.yml`)
```powershell
# Levantar m2-postgres, m2-rabbitmq, m2-mailpit y m2-citas (:8081)
./m2-integracion/scripts/levantar.ps1

# Reservar cita -> publica el evento cita.reservada en RabbitMQ (cola notificaciones.cita.reservada)
$body = [System.Text.Encoding]::UTF8.GetBytes('{"dni":"40000001","nombres":"Ana Perez","email":"ana@correo.lab","oficinaId":"MAD","fecha":"2026-11-18","hora":"09:00","tipoTramite":"LEGALIZACION"}')
Invoke-RestMethod -Uri "http://localhost:8081/api/citas" -Method POST -ContentType "application/json; charset=utf-8" -Body $body | ConvertTo-Json

# Limpiar contenedores y volúmenes de M2 al terminar
./m2-integracion/scripts/limpiar.ps1
```

