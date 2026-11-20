# ADR-005: Estandarización de observabilidad con OpenTelemetry Java Agent y pila Grafana LGTM

- **Estado:** Aceptado
- **Fecha:** 2026-11-19
- **Módulo / Contexto:** M4 – Observabilidad, C4 y Gobernanza (`m4-observabilidad`) / Plataforma Consular Completa

---

## 1. Contexto

En una arquitectura de 8 microservicios (`gateway`, `bff-web`, `citas-service`, `tramites-service`, `pagos-service`, `emision-service`, `identidad-service`, `notificaciones-service`) que combinan llamadas síncronas (REST y gRPC) con mensajería asíncrona (RabbitMQ y Transactional Outbox), diagnosticar un cuello de botella o un fallo mediante logs de texto aislados por contenedor resulta inviable.

Necesitamos instrumentar los tres pilares de la observabilidad (**Trazas distribuidas**, **Métricas técnicas/de negocio** y **Logs estructurados**) cumpliendo dos restricciones clave del curso:
1. No contaminar el código de dominio ni los casos de uso con SDKs propietarios de un proveedor de monitoreo.
2. Mantener el consumo total de memoria del módulo `m4-observabilidad` dentro del presupuesto de $\le 6\text{ GB}$ de RAM en un equipo con Docker Desktop.

---

## 2. Decisión

Adoptamos el estándar abierto **OpenTelemetry (CNCF)** mediante el **Agente Java de OpenTelemetry (`2.10.0`)** acoplado a la imagen todo-en-uno **`grafana/otel-lgtm:0.8.1`** (`m4-lgtm`):

1. **Instrumentación automática por bytecode (`-javaagent:/otel/opentelemetry-javaagent.jar`):**
   - Se incorpora en la etapa de ejecución de cada `Dockerfile` sin modificar el código de dominio.
   - Captura automáticamente spans de Spring MVC, Spring Cloud Gateway, RestClient, JDBC (PostgreSQL), Spring Data Redis, gRPC y Spring AMQP (RabbitMQ), y los exporta por OTLP gRPC a `http://lgtm:4317`.
2. **Enriquecimiento semántico puntual con `@WithSpan` y Micrometer:**
   - En `SagaSolicitudTramite`, cada paso de la Saga se anota con `@WithSpan` y etiqueta los atributos de negocio `tramite.id` y `saga.paso`.
   - En `OutboxPersistenceAdapter` y `RelayOutbox`, se persiste y reinyecta la cabecera W3C `traceparent` para que la traza distribuida no se corte al cruzar la tabla `outbox` en PostgreSQL.
   - Las métricas de negocio (`consular_tramites_registrados_total{tipo}`, `consular_pagos_confirmados_total`, `consular_emision_duracion_seconds`) se registran con Micrometer detrás de puertos de salida hexagonales.
3. **Logs JSON correlacionados con Trazas (`logstash-logback-encoder`):**
   - Cada servicio emite logs JSON con `trace_id`, `span_id`, `correlationId` y `servicio`, exportados a **Loki**, permitiendo saltar en un clic desde cualquier span en **Tempo** hacia sus logs con **"Logs for this span"**.
4. **Backend unificado `grafana/otel-lgtm:0.8.1`:**
   - Empaqueta en un solo contenedor ligero (`mem_limit: 1024m`) el OpenTelemetry Collector, Tempo (trazas), Loki (logs), Prometheus (métricas) y Grafana (`:3000`) con datasources y tableros pre-aprovisionados.

---

## 3. Alternativas Consideradas

| Alternativa | Ventajas | Motivo de descarte |
| --- | --- | --- |
| **1. OpenTelemetry Java Agent + `grafana/otel-lgtm` (Elegida)** | Estándar abierto neutral al proveedor (W3C `traceparent`, OTLP), instrumentación sin ensuciar el dominio, correlación nativa Trace-to-Logs y huella de memoria total del módulo de $\approx 3{,}17\text{ GB}$ ($\le 6\text{ GB}$). | Requiere puente explícito de `traceparent` en la tabla `outbox` para unir el hilo `@Scheduled` con la traza original. |
| **2. Pila ELK (Elasticsearch + Logstash + Kibana) + Jaeger separados** | Ampliamente conocida en despliegues históricos. | Solo Elasticsearch + Kibana + Jaeger consumen más de $3{,}5\text{ GB}$ de RAM adicionales, superando el presupuesto de $6\text{ GB}$ del laboratorio y careciendo de integración nativa unificada para métricas PromQL y SLOs. |
| **3. Instrumentación 100 % manual con el SDK de OpenTelemetry en cada clase** | Control fino de cada span sin agente Java. | Introduce código repetitivo de infraestructura (`Tracer`, `Scope`, `Context`) dentro de los servicios de aplicación y adaptadores, restando claridad didáctica a la arquitectura hexagonal. |

---

## 4. Consecuencias y Evidencia

- **Positivas:**
  - Visibilidad completa de extremo a extremo (Gateway → BFF → Identidad/PIDE → Trámites → Outbox → RabbitMQ → Pagos/Emisión/Notificaciones) en Grafana Tempo.
  - Identificación inmediata de cuellos de botella (como el experimento `LATENCIA_INYECTADA_MS=3000` en `pagos-service`) tanto en la cascada de spans de Tempo como en el panel de latencia `p95` de Grafana.
- **Negativas / Riesgos aceptados:**
  - El agente Java añade $\approx 30-45\text{ MB}$ de RAM por contenedor JVM y unos segundos adicionales al tiempo de arranque inicial.
- **Evidencia en el repositorio:**
  - `m4-observabilidad/docker-compose.yml` (`x-otel-common` y servicio `lgtm`).
  - `m4-observabilidad/grafana/dashboards/vision-general.json` y `flujo-tramite.json`.
  - `docs/observabilidad/slo.md` y `docs/estandares/estandar-observabilidad.md`.

