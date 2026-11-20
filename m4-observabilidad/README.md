# Módulo 4 (`m4-observabilidad`) — Observabilidad, Modelo C4 y Gobernanza Técnica

Este módulo es una copia autosuficiente de la plataforma consular que incorpora instrumentación con **OpenTelemetry**, la pila **Grafana LGTM** (Loki + Grafana + Tempo + Mimir/Prometheus), trazabilidad distribuida de extremo a extremo (incluyendo propagación por RabbitMQ y Transactional Outbox) y registros estructurados en **JSON** correlacionados con `trace_id` y `span_id`.

---

## 1. Arquitectura de Observabilidad (Sesión 6)

1. **Agente Java de OpenTelemetry (`2.10.0`)**:
   - Descargado en la etapa de compilación de cada `Dockerfile` en `/otel/opentelemetry-javaagent.jar` y activado con `JAVA_TOOL_OPTIONS=-javaagent:/otel/opentelemetry-javaagent.jar`.
   - Exporta trazas, métricas y logs por OTLP gRPC hacia `http://lgtm:4317` (`OTEL_SERVICE_NAME=<servicio>`, `OTEL_RESOURCE_ATTRIBUTES=deployment.environment=laboratorio,service.namespace=consular`).
2. **Propagación de Contexto W3C (`traceparent`) por HTTP, Outbox y RabbitMQ**:
   - **Tramos síncronos HTTP**: el agente propaga automáticamente la cabecera `traceparent` entre `gateway` (`:8080`) → `bff-web` (`:8085`) → `identidad-service` (`:8086`), `tramites-service` (`:8082`) y `pagos-service` (`:8083`).
   - **Puente sobre Transactional Outbox (`tramites.outbox` y `pagos.outbox`)**: como los eventos se persisten primero en PostgreSQL y se publican asíncronamente cada 500 ms por un hilo `@Scheduled` (`RelayOutbox`), `OutboxPersistenceAdapter` captura el `trace_id` y `span_id` activos del MDC en la columna `traceparent` (`00-<trace_id>-<span_id>-01`) y `RelayOutbox` inyecta esa cabecera `traceparent` en las propiedades del mensaje AMQP al publicarlo en RabbitMQ. Así, los consumidores de `tramites-service`, `emision-service` y `notificaciones-service` continúan la misma traza distribuida.
3. **Logs JSON correlacionados con Trazas (`logback-spring.xml`)**:
   - Todos los servicios usan `LogstashEncoder` para emitir logs JSON con `@timestamp`, `servicio`, `nivel`, `mensaje`, `trace_id`, `span_id` y `correlationId`.
   - En **Grafana** (`http://localhost:3000`, `admin` / `admin`), el datasource **Tempo** está preconfigurado con *Trace to Logs* hacia **Loki**, permitiendo saltar desde cualquier span de una traza a sus logs con el botón **"Logs for this span"**.

---

## 2. Comandos Rápidos

```powershell
# Levantar todos los contenedores de M4 y esperar estado healthy
cd m4-observabilidad
.\scripts\levantar.ps1

# Detener contenedores conservando volúmenes
.\scripts\detener.ps1

# Eliminar contenedores, redes y volúmenes
.\scripts\limpiar.ps1 -Forzar
```

---

## 3. Verificación Rápida de Trazas y Logs en Grafana

1. Levantar el módulo con `.\scripts\levantar.ps1`.
2. Iniciar un trámite en `http://localhost:4200` (o vía `POST http://localhost:8080/bff/tramites`) y registrar su pago (`POST http://localhost:8080/api/pagos` con `Idempotency-Key`).
3. Abrir Grafana en `http://localhost:3000` (`admin` / `admin`) → **Explore** → seleccionar datasource **Tempo**:
   - Buscar trazas de `gateway`, `tramites-service`, `pagos-service`, `emision-service` o `notificaciones-service`.
   - Abrir la traza del trámite y hacer clic en el icono de logs (**"Logs for this span"**) para inspeccionar en **Loki** las líneas JSON con el mismo `trace_id`.

---

## 4. Métricas de Negocio, Tableros, SLO y Latencia Inyectada (Sesión 6)

### 4.1 Métricas de Negocio (Micrometer → Prometheus)
- `consular_tramites_registrados_total{tipo}` (`tramites-service`): contador de trámites registrados por tipo (`LEGALIZACION`, `APOSTILLA`, `REGISTRO_CONSULAR`).
- `consular_pagos_confirmados_total` (`pagos-service`): contador de pagos confirmados.
- `consular_emision_duracion_seconds` (`emision-service`): histograma de duración del proceso de emisión documental.
- `consular_tramites_por_estado{estado}` (`tramites-service`): gauge en tiempo real de trámites por estado en `tramites.vista_tramite`.

### 4.2 Spans Manuales de la Saga (`@WithSpan`)
En `SagaSolicitudTramite.java`, cada paso del orquestador (`iniciarSaga`, `alConfirmarPago`, `alCompletarEmision`, `alFallarEmision`) está anotado con `@WithSpan` y registra los atributos `tramite.id` y `saga.paso` (`ESPERANDO_PAGO`, `EMISION_SOLICITADA`, `EMISION_COMPLETADA`, `EMISION_FALLIDA`).

### 4.3 Tableros Aprovisionados y SLO
Al iniciar Grafana (`http://localhost:3000`, `admin` / `admin`), en la carpeta **Plataforma Consular** aparecen automáticamente:
1. **"Plataforma consular – visión general"** (`vision-general.json`): panel Gauge del **SLI/SLO** (`GET /api/tramites/{id}` < 500 ms ≥ 99 %, documentado en `docs/observabilidad/slo.md`), RPS, latencia p95 y errores por servicio, trámites registrados por tipo y pagos confirmados.
2. **"Flujo de trámite"** (`flujo-tramite.json`): trámites por estado (CQRS) y percentiles p50/p95/p99 de duración de emisión.

### 4.4 Experimento de Latencia Inyectada en `pagos-service` (Sin Reconstruir la Imagen)
El servicio `pagos-service` lee la variable de entorno `LATENCIA_INYECTADA_MS` (propiedad `latencia.inyectada.ms`, por defecto `0`). Para activar o desactivar 3000 ms de latencia en caliente sin reconstruir la imagen Docker:

```powershell
# 1. Activar latencia de 3000 ms en pagos-service (recrea solo m4-pagos en ~2 segundos)
$env:LATENCIA_INYECTADA_MS = "3000"
docker compose -f m4-observabilidad/docker-compose.yml up -d pagos-service

# 2. Registrar un pago (tardará ~3 segundos; observar el span de pagos-service en Tempo y la curva p95 en Grafana)

# 3. Desactivar la latencia inyectada volviendo a 0 ms
Remove-Item Env:LATENCIA_INYECTADA_MS -ErrorAction SilentlyContinue
docker compose -f m4-observabilidad/docker-compose.yml up -d pagos-service
```

---

## 5. Gobernanza Técnica, Validación de APIs y Calidad con SonarQube (Sesión 6)

### 5.1 Estándares y Validación Automatizada de APIs con Spectral
Los lineamientos de arquitectura y gobierno digital se encuentran en [docs/estandares/](file:///c:/Jorge-Projects/mre_arquitectura/mrree-arquitectura-software/docs/estandares/):
- `lineamientos-api.md` (borrador v0.1 alineado con D.L. N.° 1412 y PIDE) y `.spectral.yaml` (`rutas-en-kebab-case`).
- `estandar-observabilidad.md`, `reglas-arquitectura.md` y `checklist-revision-arquitectura.md`.

Para validar todos los contratos OpenAPI con **Spectral** en contenedor:
```powershell
cd m4-observabilidad
.\scripts\validar-apis.ps1
```

### 5.2 Perfil `calidad` con SonarQube Community Build (`:9000`)
> **Advertencia de recursos:** El contenedor `m4-sonarqube` (`sonarqube:26.11.0.134175-community`) consume aproximadamente **2 GB de RAM** adicionales. Por ello se encuentra aislado bajo el perfil opcional `calidad` y está pensado para la demostración del docente en la Sesión 6 (puede ejecutarse sin levantar el resto de los microservicios).

```powershell
cd m4-observabilidad
.\scripts\analizar-calidad.ps1
```
Una vez completado el análisis con el scanner de Maven en contenedor, abra:
- **SonarQube Dashboard:** `http://localhost:9000/dashboard?id=consular-tramites-service`

---

## 6. Ejecución en Modo Híbrido desde Eclipse

Cada microservicio incluye `src/main/resources/application-local.yml` con los hosts apuntando a `localhost`. Para depurar un servicio desde **Spring Tools for Eclipse**:
1. Levantar la infraestructura con `.\scripts\levantar.ps1` y detener únicamente el contenedor del servicio a depurar (`docker stop m4-tramites`).
2. En Eclipse: *Run As → Spring Boot App* con el perfil activo `local` (`-Dspring.profiles.active=local`).
