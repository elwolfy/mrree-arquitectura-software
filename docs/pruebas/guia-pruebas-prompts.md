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
| **07 (Parte 1)** | M1: Compose único con perfiles y scripts robustos | `m1-estilos/docker-compose.yml`, `m1-estilos/scripts/*` | Completado |
| **07 (Parte 2)** | M1: Scripts k6 de carga y matrices comparativas | `docs/pruebas/k6`, `docs/matrices/*` | Completado |
| **07 (Parte 3)** | M1: `README.md`, verificación integral y tag `s1` | `m1-estilos/README.md`, tag `s1` | Completado |
| **07-1** | M1: Andamiaje y guía A1 en `main` (tag `s2`) + solución en `solucion/a1` | `docs/asignaciones/A1*.md`, `docs/adr/ADR-001*.md`, `monolito-modular` | Completado |
| **08-1** | M2: Esqueleto base, RabbitMQ y `citas-service` | `m2-integracion/citas-service` | Completado |
| **08-2** | M2: `tramites-service` (Factory Method, Builder, eventos y gRPC) | `m2-integracion/tramites-service` | Completado |
| **08-3** | M2: `identidad-service` (Adapter + ACL) y `pide-simulador` | `m2-integracion/identidad-service`, `pide-simulador` | Completado |
| **08-4 a 08-7** | M2: `notificaciones`, `pagos`, `bff`, `gateway`, `sidecar` | `m2-integracion/*` | Pendiente |
| **09** | M3: Persistencia, Saga, Outbox, Event Sourcing y CQRS | `m3-datos/*` | Pendiente |
| **10** | M4: Observabilidad (OpenTelemetry + Grafana LGTM) | `m4-observabilidad/*` | Pendiente |
| **11–13** | ADRs, C4, soluciones de asignaciones y verificación final | `docs/*` | Pendiente |

---

## Prompt 01 — Bootstrap del Repositorio

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

### 2. Pruebas en contenedores Docker (`:8081` y Frontend `:4200`)
```powershell
# Levantar PostgreSQL, Mailpit, Frontend y el Monolito
.\m1-estilos\scripts\levantar.ps1 monolito

# 2.1 Información del sistema (variante: MONOLITO_MODULAR, arquitecturaInterna: HEXAGONAL)
Invoke-RestMethod -Uri "http://localhost:8081/api/sistema/info" | ConvertTo-Json

# 2.2 Listar las 6 oficinas consulares semilla
Invoke-RestMethod -Uri "http://localhost:8081/api/oficinas" | Format-Table id, ciudad, pais, zonaHoraria

# 2.3 Consultar disponibilidad de Madrid
Invoke-RestMethod -Uri "http://localhost:8081/api/oficinas/MAD/disponibilidad?fecha=2026-11-18" | ConvertTo-Json -Depth 4

# 2.4 Calcular tarifa con patrón Strategy (LEGALIZACION y APOSTILLA)
Invoke-RestMethod -Uri "http://localhost:8081/api/tarifas?tipoTramite=APOSTILLA&pais=ES" | ConvertTo-Json

# 2.5 Reservar una cita (201 Created, notificacion: ENVIADA)
$body = [System.Text.Encoding]::UTF8.GetBytes('{"dni":"40000001","nombres":"Ana Perez","email":"ana@correo.lab","oficinaId":"MAD","fecha":"2026-11-18","hora":"09:00","tipoTramite":"LEGALIZACION"}')
$cita = Invoke-RestMethod -Uri "http://localhost:8081/api/citas" -Method POST -ContentType "application/json; charset=utf-8" -Body $body
$cita | ConvertTo-Json

# 2.6 Medir arranque y memoria RAM
.\m1-estilos\scripts\medir.ps1
```

### 3. Experimento didáctico CAP (`simular-particion` y `restaurar-red`)
```powershell
# Simular partición desconectando m1-monolito de la red m1_datos
.\m1-estilos\scripts\simular-particion.ps1

# Restaurar la conexión de red
.\m1-estilos\scripts\restaurar-red.ps1

# Limpiar al terminar
.\m1-estilos\scripts\limpiar.ps1 -Forzar
```

---

## Prompt 03 — M1: Comparativa Clean Architecture (`m1-estilos/comparativa-clean`)

### 1. Pruebas automatizadas (Use Cases, Presenter, ArchUnit y Testcontainers)
```powershell
mvn -B -f m1-estilos verify -pl comparativa-clean
```

### 2. Pruebas en contenedores Docker (`levantar.ps1 clean`)
```powershell
# Levantar variante Clean Architecture (:8081) + Frontend (:4200)
.\m1-estilos\scripts\levantar.ps1 clean

# Verificar que arquitecturaInterna reporta CLEAN
Invoke-RestMethod -Uri "http://localhost:8081/api/sistema/info" | ConvertTo-Json

# Reservar cita con DNI 40000001
$body = [System.Text.Encoding]::UTF8.GetBytes('{"dni":"40000001","nombres":"Ana Perez","email":"ana@correo.lab","oficinaId":"MAD","fecha":"2026-11-18","hora":"09:30","tipoTramite":"LEGALIZACION"}')
Invoke-RestMethod -Uri "http://localhost:8081/api/citas" -Method POST -ContentType "application/json; charset=utf-8" -Body $body | ConvertTo-Json

# Medir arranque y memoria
.\m1-estilos\scripts\medir.ps1

# Limpiar al terminar
.\m1-estilos\scripts\limpiar.ps1 -Forzar
```

---

## Prompt 04 — M1: Variante Microservicios (`m1-estilos/microservicios`)

### 1. Pruebas automatizadas (Unitarias + WireMock Circuit Breaker + Testcontainers)
```powershell
mvn -B -f m1-estilos verify -pl microservicios/citas-service,microservicios/notificaciones-service
```

### 2. Pruebas en contenedores Docker (`levantar.ps1 microservicios`)
```powershell
# Levantar postgres, mailpit, frontend, m1-notificaciones (:8084) y m1-citas (:8081)
.\m1-estilos\scripts\levantar.ps1 microservicios

# 2.1 Verificar info del sistema (variante: MICROSERVICIOS)
Invoke-RestMethod -Uri "http://localhost:8081/api/sistema/info" | ConvertTo-Json

# 2.2 Reservar cita con m1-notificaciones activo -> responde 201 con notificacion: ENVIADA
$body1 = [System.Text.Encoding]::UTF8.GetBytes('{"dni":"40000001","nombres":"Ana Perez","email":"ana@correo.lab","oficinaId":"MAD","fecha":"2026-11-18","hora":"09:30","tipoTramite":"LEGALIZACION"}')
Invoke-RestMethod -Uri "http://localhost:8081/api/citas" -Method POST -ContentType "application/json; charset=utf-8" -Body $body1 | ConvertTo-Json

# 2.3 Prueba de resiliencia (Circuit Breaker + Timeout 2 s): detener m1-notificaciones
docker stop m1-notificaciones

# Reservar otra cita -> responde 201 en < 3 s con notificacion: PENDIENTE
$body2 = [System.Text.Encoding]::UTF8.GetBytes('{"dni":"40000002","nombres":"Maria Diaz","email":"maria@correo.lab","oficinaId":"MAD","fecha":"2026-11-18","hora":"10:00","tipoTramite":"APOSTILLA"}')
Invoke-RestMethod -Uri "http://localhost:8081/api/citas" -Method POST -ContentType "application/json; charset=utf-8" -Body $body2 | ConvertTo-Json

# Limpiar al terminar
.\m1-estilos\scripts\limpiar.ps1 -Forzar
```

---

## Prompt 05 — M1: Variante Serverless (`m1-estilos/serverless`)

### 1. Pruebas automatizadas (Funciones unitarias sin Spring + Testcontainers)
```powershell
mvn -B -f m1-estilos verify -pl serverless/funciones-citas
```

### 2. Pruebas en contenedores Docker (`levantar.ps1 serverless`)
```powershell
# Levantar postgres, mailpit, frontend, 3 contenedores de funciones y m1-enrutador (:8081)
.\m1-estilos\scripts\levantar.ps1 serverless

# 2.1 Medir tiempos de arranque en frío ("Función lista en N ms") y memoria
.\m1-estilos\scripts\medir.ps1

# 2.2 Verificar cabecera X-Funcion-Invocada e infoSistema (variante: SERVERLESS)
$respInfo = Invoke-WebRequest -Uri "http://localhost:8081/api/sistema/info" -UseBasicParsing
Write-Host "X-Funcion-Invocada: $($respInfo.Headers['X-Funcion-Invocada']) | Body: $($respInfo.Content)"

# 2.3 Reservar cita con DNI 40000001
$body1 = [System.Text.Encoding]::UTF8.GetBytes('{"dni":"40000001","nombres":"Ana Perez","email":"ana@correo.lab","oficinaId":"MAD","tipoTramite":"LEGALIZACION","fecha":"2026-11-18","hora":"11:30"}')
$respCita1 = Invoke-WebRequest -Uri "http://localhost:8081/api/citas" -Method POST -ContentType "application/json; charset=utf-8" -Body $body1 -UseBasicParsing
Write-Host "Status: $($respCita1.StatusCode) | X-Funcion-Invocada: $($respCita1.Headers['X-Funcion-Invocada']) | Body: $($respCita1.Content)"

# 2.4 Detener m1-fn-notificar -> la reserva responde 201 en ~300 ms con notificacion: PENDIENTE
docker stop m1-fn-notificar
$body2 = [System.Text.Encoding]::UTF8.GetBytes('{"dni":"40000002","nombres":"Carlos Mendoza","email":"carlos@correo.lab","oficinaId":"MAD","tipoTramite":"APOSTILLA","fecha":"2026-11-18","hora":"12:00"}')
$respCita2 = Invoke-WebRequest -Uri "http://localhost:8081/api/citas" -Method POST -ContentType "application/json; charset=utf-8" -Body $body2 -UseBasicParsing
Write-Host "Status: $($respCita2.StatusCode) | Body: $($respCita2.Content)"

# Limpiar al terminar
.\m1-estilos\scripts\limpiar.ps1 -Forzar
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

### 2. Prueba interactiva en el navegador (`http://localhost:4200`) con `levantar.ps1`
```powershell
# Levanta automáticamente m1-postgres, m1-mailpit, m1-monolito (:8081) y m1-frontend (:4200)
.\m1-estilos\scripts\levantar.ps1 monolito
```
Abre `http://localhost:4200` en el navegador para recorrer el flujo de reserva en 5 pasos, consultar citas, verificar el banner RFC 9457 (`409 Cita duplicada`) y, al terminar, ejecuta:
```powershell
.\m1-estilos\scripts\limpiar.ps1 -Forzar
```

---

## Prompt 07 (Parte 1) — Compose Único con Perfiles y Scripts de M1 (`m1-estilos/scripts`)

### Catálogo de scripts disponibles en `m1-estilos/scripts`:
```powershell
# Levantar cualquiera de las 4 variantes (detiene la anterior y reinicia esquemas limpiamente)
.\m1-estilos\scripts\levantar.ps1 monolito
.\m1-estilos\scripts\levantar.ps1 clean
.\m1-estilos\scripts\levantar.ps1 microservicios
.\m1-estilos\scripts\levantar.ps1 serverless

# Medir tiempos de arranque en logs y consumo de RAM (docker stats)
.\m1-estilos\scripts\medir.ps1

# Ejecutar pruebas de carga k6 sobre la variante en ejecución (50 VUs por 60 s, o rampa 10->100 VUs)
.\m1-estilos\scripts\carga.ps1 citas
.\m1-estilos\scripts\carga.ps1 pico

# Simular y restaurar partición de red sobre m1_datos (para monolito o clean)
.\m1-estilos\scripts\simular-particion.ps1
.\m1-estilos\scripts\restaurar-red.ps1

# Detener contenedores sin borrar volúmenes
.\m1-estilos\scripts\detener.ps1

# Eliminar contenedores, redes y volúmenes de M1
.\m1-estilos\scripts\limpiar.ps1 -Forzar
```

### Matrices de trabajo en `docs/matrices/`:
- `docs/matrices/m1-estilos.md`: Criterios cualitativos y tabla de medición experimental para S1.
- `docs/matrices/m1-escenarios-calidad.md`: Plantilla de 6 partes, ejemplo consular resuelto y árbol de utilidad.
- `docs/matrices/m1-cap-por-operacion.md`: Decisiones CAP por operación y espacio vacío para `Reprogramar cita` (Asignación A1).
- `docs/matrices/m1-hexagonal-vs-clean.md`: Equivalencias clase por clase entre Hexagonal y Clean Architecture.

---

## Prompt 07-1 — Guía y Solución de la Asignación A1 (`main` y `solucion/a1`)

### 1. En la rama `main` (Andamiaje de A1 — Etiqueta `s2`)
```powershell
git checkout main

# 1. Compilación y pruebas de todo M1 (ReglaDeReprogramacionTest aparece como skipped)
mvn -B -f m1-estilos verify

# 2. Verificar que en main no está resuelta la regla
git grep -n "Duration.between" -- m1-estilos/monolito-modular/src/main/java/**/ReglaDeReprogramacion.java

# 3. Levantar monolito y comprobar que PUT /api/citas/{id}/reprogramacion devuelve 501 Not Implemented
.\m1-estilos\scripts\levantar.ps1 monolito
$f1 = "2026-11-23"
$f2 = "2026-11-24"
$cita = Invoke-RestMethod -Uri "http://localhost:8081/api/citas" -Method Post -ContentType "application/json" -Body (@{dni="40000001";nombres="Ana Perez";email="ana@correo.lab";oficinaId="MAD";fecha=$f1;hora="09:00";tipoTramite="LEGALIZACION"} | ConvertTo-Json)
try {
    Invoke-RestMethod -Uri "http://localhost:8081/api/citas/$($cita.id)/reprogramacion" -Method Put -ContentType "application/json" -Body (@{fecha=$f2;hora="10:00"} | ConvertTo-Json)
} catch {
    Write-Host "HTTP Status:" $_.Exception.Response.StatusCode.value__
}
```

### 2. En la rama `solucion/a1` (Solución de Referencia)
```powershell
git checkout solucion/a1

# 1. Ejecutar prueba unitaria de la regla de 24 h (3/3 tests en verde) y verificación completa
mvn -B -f m1-estilos verify -pl monolito-modular

# 2. Verificar que git diff --stat main...solucion/a1 muestra solo los 4 archivos de la solución
git diff --stat main...solucion/a1

# 3. Levantar monolito en solucion/a1 y probar reprogramación >= 24 h (200 REPROGRAMADA) y < 24 h (422)
.\m1-estilos\scripts\levantar.ps1 monolito
```

---

## Prompt 08-1 — M2: Base de Integración y `citas-service` con RabbitMQ (`m2-integracion/`)

```powershell
# 1. Pruebas automatizadas
mvn -B -f m2-integracion verify

# 2. Levantar entorno de M2
.\m2-integracion\scripts\levantar.ps1

# 3. Reservar cita -> publica evento cita.reservada en RabbitMQ
$body = [System.Text.Encoding]::UTF8.GetBytes('{"dni":"40000001","nombres":"Ana Perez","email":"ana@correo.lab","oficinaId":"MAD","fecha":"2026-11-18","hora":"09:00","tipoTramite":"LEGALIZACION"}')
Invoke-RestMethod -Uri "http://localhost:8081/api/citas" -Method POST -ContentType "application/json; charset=utf-8" -Body $body | ConvertTo-Json

# 4. Limpiar entorno de M2
.\m2-integracion\scripts\limpiar.ps1
```

---

## Prompt 08-2 — M2: `tramites-service` (Factory Method, Builder, Eventos y gRPC)

### 1. Pruebas automatizadas (Unitarias + ArchUnit + Testcontainers PostgreSQL/RabbitMQ + gRPC)
```powershell
mvn -B -f m2-integracion verify
git grep -n "switch\|instanceof" m2-integracion/tramites-service/src/main/java/**/application
```

### 2. Pruebas en contenedores Docker (`:8082` HTTP y `:9082` gRPC)
```powershell
# Levantar PostgreSQL, RabbitMQ, Mailpit, citas-service (:8081) y tramites-service (:8082 / :9082)
.\m2-integracion\scripts\levantar.ps1

# 2.1 Registrar los 3 tipos de trámite (verifica subtipo y estado PENDIENTE_PAGO)
Invoke-RestMethod -Uri "http://localhost:8082/api/tramites" -Method POST -ContentType "application/json; charset=utf-8" -Body ([System.Text.Encoding]::UTF8.GetBytes('{"dni":"40000001","nombres":"Ana Perez","email":"ana@correo.lab","tipoTramite":"LEGALIZACION","pais":"ES","folios":3,"idioma":"ES"}')) | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8082/api/tramites" -Method POST -ContentType "application/json; charset=utf-8" -Body ([System.Text.Encoding]::UTF8.GetBytes('{"dni":"40000002","nombres":"Carlos Mendoza","email":"carlos@correo.lab","tipoTramite":"APOSTILLA","pais":"ES","folios":2,"idioma":"ES"}')) | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8082/api/tramites" -Method POST -ContentType "application/json; charset=utf-8" -Body ([System.Text.Encoding]::UTF8.GetBytes('{"dni":"40000003","nombres":"Elena Vargas","email":"elena@correo.lab","tipoTramite":"REGISTRO_CONSULAR","pais":"AR","folios":1,"idioma":"ES"}')) | ConvertTo-Json

# 2.2 Detener contenedores al terminar
.\m2-integracion\scripts\detener.ps1
```

---

## Prompt 08-3 — M2: `identidad-service` (Adapter + ACL) y `pide-simulador`

### 1. Pruebas automatizadas y verificación de encapsulamiento de la ACL
```powershell
mvn -B -f m2-integracion verify
git grep -n "coResultado" m2-integracion/identidad-service/src/main
```

### 2. Pruebas en contenedores Docker (`:8086` `identidad-service` y `:8089` `pide-simulador`)
```powershell
# Levantar entorno M2
.\m2-integracion\scripts\levantar.ps1

# 2.1 DNI válido (40000001) -> 200 OK con Ciudadano
Invoke-RestMethod -Uri "http://localhost:8086/api/identidad/validaciones" -Method POST -ContentType "application/json" -Body '{"dni":"40000001"}' | ConvertTo-Json

# 2.2 DNI fallecido (49999999) -> 422 "Identidad no vigente"
try { Invoke-WebRequest -Uri "http://localhost:8086/api/identidad/validaciones" -Method POST -ContentType "application/json" -Body '{"dni":"49999999"}' -UseBasicParsing } catch { Write-Host "HTTP:" ([int]$_.Exception.Response.StatusCode) }

# 2.3 DNI no encontrado (48888888) -> 404 "DNI no encontrado"
try { Invoke-WebRequest -Uri "http://localhost:8086/api/identidad/validaciones" -Method POST -ContentType "application/json" -Body '{"dni":"48888888"}' -UseBasicParsing } catch { Write-Host "HTTP:" ([int]$_.Exception.Response.StatusCode) }

# 2.4 Detener contenedores al terminar
.\m2-integracion\scripts\detener.ps1
```
