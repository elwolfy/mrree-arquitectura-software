# Prompt 10 — M4: Documentación, observabilidad y gobernanza

> Rama `main`. Referencias: contrato (sección 7), reglas 01–04, plan sección 7 (4.1–4.4) y sección 8 (S5, S6).

## Objetivo didáctico

La solución completa de M3 instrumentada con OpenTelemetry y visualizada en Grafana; modelo C4 navegable en Structurizr Lite; herramientas para la demostración de deuda técnica y gobernanza.

`m4-observabilidad/` es autosuficiente (copia de M3 + instrumentación). Compose `name: m4`.

## Tareas

### 1. Instrumentación

- Descargar el agente Java de OpenTelemetry en una etapa del Dockerfile (versión fijada) y activarlo con `JAVA_TOOL_OPTIONS`. Variables `OTEL_SERVICE_NAME`, `OTEL_EXPORTER_OTLP_ENDPOINT=http://lgtm:4317`, `OTEL_RESOURCE_ATTRIBUTES=deployment.environment=laboratorio,service.namespace=consular`.
- Propagación del contexto por RabbitMQ (cabecera `traceparent`): verificar que la traza de un trámite cruza gateway → bff → tramites → (broker) → pagos/emision/notificaciones. Si la instrumentación automática no cubre algún tramo, agregar propagación manual y documentarlo.
- Logs JSON (logstash-logback-encoder) con `trace_id`, `span_id`, `servicio`; exportados a Loki vía OTLP.
- Métricas de negocio Micrometer del contrato; spans manuales con `@WithSpan` en `SagaSolicitudTramite` (cada paso como span con atributos `tramite.id`, `saga.paso`).
- `latencia.inyectada.ms` en pagos (escenario de clase: 3000).

### 2. Grafana LGTM

- Servicio `lgtm` (`grafana/otel-lgtm`, `m4-lgtm`, 3000, 4317, 4318).
- Aprovisionar tableros desde `grafana/dashboards/`: "Plataforma consular – visión general" (solicitudes/s, p95 y errores por servicio; trámites registrados por tipo; pagos confirmados) y "Flujo de trámite" (duración de emisión, estados).
- Correlación traza ↔ logs configurada en las fuentes de datos (Tempo → Loki por `trace_id`).
- `docs/observabilidad/slo.md`: SLI y SLO de la consulta de estado (contrato), consulta PromQL del SLI y panel correspondiente.

### 3. Structurizr Lite (C4)

- Servicio `structurizr` (`m4-structurizr`, 8090) que monta `../docs/c4`.
- El modelo se crea en el prompt 11; aquí solo el servicio y el montaje.

### 4. Gobernanza y deuda técnica

- Perfil `calidad`: SonarQube Community (`m4-sonarqube`, 9000) + script `scripts/analizar-calidad.(ps1|sh)` que ejecuta el análisis de `tramites-service` con el scanner de Maven en contenedor. Advertir en el README el consumo de memoria (≈ 2 GB) y que es para la demostración del docente.
- `docs/estandares/`: `lineamientos-api.md` (borrador 0.1 que se completa en clase: nombres, versionamiento, RFC 9457, paginación, seguridad, idempotencia), `estandar-observabilidad.md`, `reglas-arquitectura.md` (enlaza a las pruebas ArchUnit), `checklist-revision-arquitectura.md`. Mencionar alineamiento con el marco de Gobierno Digital del Estado peruano (D. Leg. 1412) y uso de la PIDE.
- Validación de contratos OpenAPI con un linter (Spectral en contenedor `stoplight/spectral`) mediante `scripts/validar-apis.(ps1|sh)` y reglas `docs/estandares/.spectral.yaml` derivadas de los lineamientos.

### 5. Rama `demo/strangler` (opcional, al final)

Desde `main`, crear `demo/strangler` donde el gateway de M4 enruta `/api/citas/**` al monolito de M1 empaquetado como contenedor adicional y, mediante un predicado de ruta por cabecera `X-Version: nueva`, al citas-service. README de la demostración.

## Criterios de aceptación

- `/verificar-modulo m4-observabilidad` pasa con presupuesto de memoria ≤ 6 GB (sin perfil `calidad`).
- Un trámite creado desde el frontend genera una traza completa visible en Grafana (Explore → Tempo) que incluye los tramos asíncronos; desde la traza se salta a los logs.
- Con `LATENCIA_INYECTADA_MS=3000` el tramo lento de pagos-service se identifica en la traza y en el panel de p95.
- Los tableros aparecen aprovisionados al iniciar.
- `http://localhost:8090` abre Structurizr Lite (aunque el modelo aún esté vacío).
- `scripts/validar-apis` se ejecuta y reporta resultados.

## Commit

`feat(m4): observabilidad`
