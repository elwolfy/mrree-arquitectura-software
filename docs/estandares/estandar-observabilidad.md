# Estándar de Observabilidad — Plataforma Consular (M4)

- **Marco de Referencia:** Pilares de Observabilidad (Logs, Métricas y Trazas) con **OpenTelemetry** (CNCF) y la pila **Grafana LGTM** (Loki, Grafana, Tempo, Mimir/Prometheus).
- **Documento de SLO asociado:** [docs/observabilidad/slo.md](file:///c:/Jorge-Projects/mre_arquitectura/mrree-arquitectura-software/docs/observabilidad/slo.md).

---

## 1. Instrumentación Estándar con OpenTelemetry

Todos los microservicios Java de la plataforma se ejecutan con el agente de instrumentación automática de OpenTelemetry (`/otel/opentelemetry-javaagent.jar`, versión `2.10.0`) y deben declarar las siguientes variables de entorno en `docker-compose.yml`:

| Variable | Valor requerido | Propósito |
| --- | --- | --- |
| `JAVA_TOOL_OPTIONS` | `-javaagent:/otel/opentelemetry-javaagent.jar` | Activa la instrumentación bytecode sin acoplar el dominio. |
| `OTEL_SERVICE_NAME` | `<nombre-del-servicio>` (p. ej. `tramites-service`) | Identifica al servicio en Tempo, Loki y Prometheus. |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | `http://lgtm:4317` | Endpoint OTLP gRPC del colector en `m4-lgtm`. |
| `OTEL_RESOURCE_ATTRIBUTES` | `deployment.environment=laboratorio,service.namespace=consular` | Etiquetas de recurso institucionales. |

### Propagación del Contexto (`traceparent`)
- **HTTP / gRPC:** Propagación automática del estándar W3C Trace Context (`traceparent`).
- **Mensajería Asíncrona y Transactional Outbox:** Todo registro insertado en tablas `outbox` (`tramites.outbox`, `pagos.outbox`) debe persistir el `traceparent` activo del hilo de negocio para que `RelayOutbox` lo adjunte como cabecera AMQP al publicar el mensaje en RabbitMQ.

---

## 2. Estándar de Registros Estructurados (Logs JSON)

Todos los servicios deben utilizar `logstash-logback-encoder` (`src/main/resources/logback-spring.xml`) emitiendo una línea JSON por evento en `stdout`:

### Campos obligatorios en cada línea de log JSON
- `@timestamp`: Marca de tiempo en formato ISO-8601 con zona horaria.
- `servicio`: Nombre del microservicio (`spring.application.name`).
- `entorno`: `laboratorio` (o el entorno de despliegue).
- `nivel`: `INFO`, `WARN`, `ERROR`, `DEBUG`.
- `mensaje`: Descripción en español, en tercera persona y con identificadores de negocio (`codigo`, `tramiteId`, `pagoId`).
- `trace_id` y `span_id`: Inyectados en el MDC por el agente de OpenTelemetry para habilitar el salto directo **"Logs for this span"** desde Tempo hacia Loki en Grafana.
- `correlationId`: Identificador de correlación HTTP (`X-Correlation-Id`).

### Privacidad y Protección de Datos Personales en Logs
- **DNI enmascarado:** Siempre registrar `****0001` (`Dni.enmascarado()`), nunca los 8 dígitos en claro.
- **Sin datos sensibles:** Prohibido registrar correos electrónicos completos, contraseñas o tokens en líneas de log.

---

## 3. Métricas Mínimas por Servicio (Micrometer / Prometheus)

Cada servicio debe exponer `/actuator/prometheus` y activar el histograma de percentiles sobre `http.server.requests` con cubetas SLO (`50ms,100ms,250ms,500ms,1s,2s,5s`).

### 3.1 Métricas Técnicas (RED: Rate, Errors, Duration)
- **Tasa (Rate):** `rate(http_server_requests_seconds_count{uri!~"/actuator.*"}[1m])`
- **Errores (Errors):** `rate(http_server_requests_seconds_count{status=~"4..|5.."}[1m])`
- **Duración (Duration p95):** `histogram_quantile(0.95, sum by (le, application) (rate(http_server_requests_seconds_bucket[1m])))`

### 3.2 Métricas de Negocio Obligatorias
Todo nuevo flujo de negocio (incluido el Proyecto Final) debe exponer sus indicadores de dominio a través de un puerto de salida hexagonal (`Metricas<Contexto>Port`):
1. `consular_tramites_registrados_total{tipo}` (`Counter` en `tramites-service`).
2. `consular_pagos_confirmados_total` (`Counter` en `pagos-service`).
3. `consular_emision_duracion_seconds` (`Timer` con histograma en `emision-service`).
4. `consular_tramites_por_estado{estado}` (`Gauge` sobre la proyección CQRS en `tramites-service`).

---

## 4. Objetivos de Nivel de Servicio (SLO)

Todo servicio crítico de cara al ciudadano debe definir su **SLI**, **SLO** y **Presupuesto de Error** siguiendo la especificación de [docs/observabilidad/slo.md](file:///c:/Jorge-Projects/mre_arquitectura/mrree-arquitectura-software/docs/observabilidad/slo.md):
- **SLO de Referencia de la Plataforma:** El **99,0 %** de las consultas de estado de trámite (`GET /api/tramites/{id}`) deben completarse con una latencia inferior a **500 ms**, monitoreado de forma continua en el tablero aprovisionado **"Plataforma consular – visión general"**.

