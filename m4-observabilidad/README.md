# Módulo 4 (`m4-observabilidad`) — Observabilidad con OpenTelemetry, Modelo C4 y Gobernanza Técnica

Este módulo es la versión integral y autosuficiente de la **Plataforma Digital de Servicios Consulares y de Legalización** utilizada en las **Sesiones 5 y 6**. Incorpora el **Agente Java de OpenTelemetry**, la pila **Grafana LGTM** (`m4-lgtm`: Loki + Grafana + Tempo + Mimir/Prometheus), el **Modelo C4** en Structurizr DSL (`m4-structurizr`), validación de contratos OpenAPI con **Spectral** y análisis de calidad y deuda técnica con **SonarQube Community Build** (`m4-sonarqube`).

---

## 1. Mapa de Contenedores y Puertos

| Contenedor | Servicio / Rol | Puerto(s) en Host | Perfil Compose |
| --- | --- | --- | --- |
| `m4-frontend` | Frontend Ciudadano (Angular + PrimeNG en modo `m4`) | `http://localhost:4200` | Por defecto |
| `m4-gateway` | API Gateway (Punto único de entrada y Swagger agregado) | `http://localhost:8080` | Por defecto |
| `m4-bff` | BFF Web (`/bff/inicio` y fachada `/bff/tramites`) | `http://localhost:8085` | Por defecto |
| `m4-citas` | Microservicio de Citas y Cupos Consulares | `http://localhost:8081` | Por defecto |
| `m4-tramites` | Microservicio de Trámites (Event Sourcing, CQRS, Saga, Outbox) | `http://localhost:8082` / gRPC `9082` | Por defecto |
| `m4-pagos` | Microservicio de Tarifas (Strategy) y Pagos Idempotentes | `http://localhost:8083` | Por defecto |
| `m4-notificaciones` | Microservicio de Avisos (Idempotencia, Retry, DLQ) | `http://localhost:8084` | Por defecto |
| `m4-identidad` | Microservicio de Identidad (ACL PIDE + Resilience4j) | `http://localhost:8086` | Por defecto |
| `m4-emision` | Microservicio de Emisión Documental (`DOC-YYYY-NNNNNN`) | `http://localhost:8087` | Por defecto |
| `m4-pide` | Simulador PIDE / RENIEC (WireMock) | `http://localhost:8089` | Por defecto |
| `m4-lgtm` | **Grafana LGTM** (`admin` / `admin`) + OTLP Collector | UI `http://localhost:3000` / OTLP `4317`, `4318` | Por defecto |
| `m4-postgres` | PostgreSQL 16 (Esquemas aislados por servicio) | `localhost:5432` | Por defecto |
| `m4-rabbitmq` | RabbitMQ 3 Management (`consular` / `consular`) | AMQP `5672` / Web `http://localhost:15672` | Por defecto |
| `m4-redis` | Redis 7 (Caché e Idempotencia) | `localhost:6379` | Por defecto |
| `m4-mailpit` | Mailpit (Servidor SMTP y bandeja web de laboratorio) | SMTP `1025` / Web `http://localhost:8025` | Por defecto |
| `m4-structurizr` | **Structurizr** (Modelo C4 desde `../docs/c4/workspace.dsl`) | `http://localhost:8090` | `c4` (`levantar-c4.ps1`) |
| `m4-sonarqube` | **SonarQube Community Build** (Análisis de calidad) | `http://localhost:9000` | `calidad` (`analizar-calidad.ps1`) |
| `m4-envoy` | Envoy Sidecar Proxy para `tramites-service` | Proxy `10000` / Admin `http://localhost:9901` | `sidecar` |

---

## 2. Consumo de Memoria Medido y Tiempos de Construcción

Medición obtenida con `docker stats --no-stream` con los **15 contenedores por defecto** en estado `healthy` (sin el perfil opcional `calidad`):

| Grupo de Contenedores | Memoria Medida (`MEM USAGE`) | Límite Configurado (`LIMIT`) |
| --- | --- | --- |
| 8 Microservicios Java con Agente OpenTelemetry (`citas`, `tramites`, `pagos`, `emision`, `notificaciones`, `identidad`, `bff`, `gateway`) | `~2 479 MiB` (`~200–418 MiB` c/u) | `384 MiB` – `512 MiB` c/u |
| `m4-lgtm` (Grafana + Tempo + Loki + Prometheus + OTel Collector) | `~360 MiB` | `1 024 MiB` |
| Infraestructura (`m4-postgres`, `m4-rabbitmq`, `m4-redis`, `m4-pide`, `m4-mailpit`, `m4-frontend`) | `~331 MiB` | `128 MiB` – `256 MiB` |
| **Total del Módulo 4 en ejecución** | **`~3 170 MiB` (`~3.17 GB`)** | **Cumple holgadamente el presupuesto $\le 6\text{ GB}$** |

- **Tiempo de primer `docker compose up -d --build`**: $\approx 3\text{ a } 5\text{ minutos}$ (descarga de imágenes y compilación multi-etapa con caché de BuildKit). Los arranques posteriores toman $\approx 45\text{ segundos}$.

---

## 3. Experimento de la Sesión 5 — Modelo C4 como Código (`Structurizr DSL`)

Para explorar y editar los 4 diagramas de arquitectura ([docs/c4/workspace.dsl](../docs/c4/workspace.dsl)) sin levantar toda la plataforma:

```powershell
cd m4-observabilidad
.\scripts\levantar-c4.ps1
```

1. Abrir `http://localhost:8090` y recorrer las cuatro vistas:
   - **1. Diagrama de Contexto del Sistema (C1)** (`ContextoSistema`)
   - **2. Diagrama de Contenedores (C2)** (`Contenedores`) — observe que en `main` **Redis no aparece** (es la Tarea 1 de la [Asignación A4](../docs/asignaciones/A4.md)).
   - **3. Diagrama de Componentes (C3) — `tramites-service`** (`ComponentesTramites`)
   - **4. Vista Dinámica — Flujo Solicitud → Pago → Emisión** (`FlujoSagaSolicitudPagoEmision`)
2. Validar el archivo DSL por línea de comandos:
   ```powershell
   docker run --rm -v "${PWD}/../docs/c4:/usr/local/structurizr" structurizr/cli:2025.11.09 validate -workspace /usr/local/structurizr/workspace.dsl
   ```

---

## 4. Experimentos de la Sesión 6 — Observabilidad, Gobernanza y Deuda Técnica

### 4.1 Levantar la Plataforma Completa

```powershell
cd m4-observabilidad
.\scripts\levantar.ps1
```

### 4.2 Experimento 1: Traza Distribuida de un Trámite y Salto a los Logs JSON ("Logs for this span")

1. En `http://localhost:4200` (o vía Postman / PowerShell), inicie un trámite (`POST /bff/tramites`) y pague la orden (`POST /api/pagos` con `Idempotency-Key`):
   ```powershell
   $t = Invoke-RestMethod -Uri "http://localhost:8080/bff/tramites" -Method Post -ContentType "application/json" `
     -Body '{"dni":"40000001","email":"ana@correo.lab","tipoTramite":"APOSTILLA","pais":"ES","folios":2,"idioma":"ES"}'
   Invoke-RestMethod -Uri "http://localhost:8080/api/pagos" -Method Post -ContentType "application/json" `
     -Headers @{ "Idempotency-Key" = [guid]::NewGuid().ToString() } `
     -Body "{`"tramiteId`":`"$($t.id)`",`"monto`":$($t.monto)}"
   ```
2. Abra **Grafana** en `http://localhost:3000` (usuario `admin` / clave `admin`) → menú **Explore** → seleccione el datasource **Tempo**:
   - Busque por `Service Name = gateway` o por la etiqueta de span `saga.paso` (`ESPERANDO_PAGO`, `EMISION_SOLICITADA`, `EMISION_COMPLETADA`).
   - Observe cómo la propagación de la cabecera W3C `traceparent` (persistida en `tramites.outbox` y `pagos.outbox` por `OutboxPersistenceAdapter` y reinyectada por `RelayOutbox`) permite seguir los tramos asíncronos a través de RabbitMQ.
   - Haga clic en **"Logs for this span"** sobre cualquier tramo de `tramites-service` o `pagos-service` para abrir en **Loki** las líneas de log JSON que contienen el mismo `trace_id` y `span_id`.

### 4.3 Experimento 2: Latencia Inyectada en `pagos-service` (Sin Reconstruir la Imagen)

Para simular una degradación de 3 segundos en la pasarela de pagos y observar su impacto en las trazas de Tempo y en el percentil `p95` de Grafana:

```powershell
# 1. Activar 3000 ms de latencia recreando únicamente el contenedor m4-pagos
$env:LATENCIA_INYECTADA_MS = "3000"
docker compose -f m4-observabilidad/docker-compose.yml up -d pagos-service

# 2. Registrar un nuevo pago (tardará ~3.3 s) y observar la cascada en Tempo y la curva p95 en Grafana

# 3. Restaurar la latencia a 0 ms sin reconstruir la imagen
Remove-Item Env:LATENCIA_INYECTADA_MS -ErrorAction SilentlyContinue
docker compose -f m4-observabilidad/docker-compose.yml up -d pagos-service
```

### 4.4 Experimento 3: Tableros Pre-aprovisionados y Monitoreo del SLO

En Grafana (`http://localhost:3000`) → **Dashboards → Plataforma Consular**:
1. **"Plataforma consular – visión general"** (`vision-general.json`):
   - Incluye el medidor (**Gauge**) del **SLI / SLO** definido en [docs/observabilidad/slo.md](../docs/observabilidad/slo.md) (proporción de consultas `GET /api/tramites/{id}` con latencia $< 500\text{ ms}$, objetivo $\ge 99{,}0\%$).
   - Paneles de RPS, latencia `p95` y errores por servicio, además de los contadores de negocio `consular_tramites_registrados_total{tipo}` y `consular_pagos_confirmados_total`.
2. **"Flujo de trámite"** (`flujo-tramite.json`):
   - Cantidad de trámites por estado en la proyección CQRS (`consular_tramites_por_estado{estado}`) y percentiles `p50 / p95 / p99` del histograma `consular_emision_duracion_seconds`.

### 4.5 Experimento 4: Validación Automatizada de APIs con Spectral

Valida los contratos OpenAPI de los servicios frente a las reglas de [docs/estandares/.spectral.yaml](../docs/estandares/.spectral.yaml) (derivadas de [docs/estandares/lineamientos-api.md](../docs/estandares/lineamientos-api.md)):

```powershell
cd m4-observabilidad
.\scripts\validar-apis.ps1
```

### 4.6 Experimento 5: Análisis de Calidad y Deuda Técnica con SonarQube (`perfil calidad`)

> **Advertencia de memoria:** El contenedor `m4-sonarqube` consume $\approx 2\text{ GB}$ de RAM. Se recomienda ejecutarlo con los demás servicios detenidos o en equipos con $\ge 12\text{ GB}$ de RAM, como parte de la demostración del docente.

```powershell
cd m4-observabilidad
.\scripts\analizar-calidad.ps1
```
- Resultados publicados en: `http://localhost:9000/dashboard?id=consular-tramites-service`

### 4.7 Experimento 6: Demostración del Patrón Strangler Fig (`rama demo/strangler`)

En la rama `demo/strangler`, el API Gateway de M4 enruta por defecto `/api/citas/**`, `/api/oficinas/**` y `/api/sistema/**` al monolito heredado de M1 (`m4-monolito-legado`), y desvía hacia el nuevo microservicio `citas-service` (`m4-citas`) las peticiones que incluyen la cabecera `X-Version: nueva`:

```powershell
git checkout demo/strangler
docker compose -f m4-observabilidad/docker-compose.yml up -d --build postgres rabbitmq mailpit lgtm citas-service monolito-legado gateway

# Sin cabecera -> responde MONOLITO_MODULAR
curl.exe -s http://localhost:8080/api/sistema/info

# Con cabecera X-Version: nueva -> responde MICROSERVICIOS (citas-service)
curl.exe -s -H "X-Version: nueva" http://localhost:8080/api/sistema/info

.\m4-observabilidad\scripts\limpiar.ps1 -Forzar
git checkout main
```

---

## 5. Ejecución en Modo Híbrido desde Eclipse y Solución de Problemas

### 5.1 Modo Híbrido desde Spring Tools for Eclipse
1. Levante la plataforma con `.\scripts\levantar.ps1` y detenga solo el contenedor del servicio que desea ejecutar en su IDE (p. ej. `docker stop m4-tramites`).
2. En Eclipse: clic derecho sobre el proyecto → **Run As → Spring Boot App** con el perfil activo `local` (`-Dspring.profiles.active=local`).

### 5.2 Solución de Problemas Frecuentes
- **Conflicto de puertos (`8080`, `8081`, `4200`, `5432`, `5672`)**: `levantar.ps1` detecta automáticamente si hay contenedores activos de `m1-*`, `m2-*` o `m3-*`. Ejecute `.\m3-datos\scripts\limpiar.ps1 -Forzar` antes de iniciar M4.
- **Trazas o métricas tardan unos segundos en aparecer en Grafana**: el agente OpenTelemetry y Prometheus exportan/raspan lotes cada `5 segundos`; aguarde 5–10 segundos tras ejecutar una operación y pulse **Run query** / **Refresh** en Grafana.
- **Reiniciar todo el estado de M4 desde cero**: ejecute `.\scripts\limpiar.ps1 -Forzar` para eliminar contenedores, redes y volúmenes.
