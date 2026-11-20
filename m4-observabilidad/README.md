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

## 4. Ejecución en Modo Híbrido desde Eclipse

Cada microservicio incluye `src/main/resources/application-local.yml` con los hosts apuntando a `localhost`. Para depurar un servicio desde **Spring Tools for Eclipse**:
1. Levantar la infraestructura con `.\scripts\levantar.ps1` y detener únicamente el contenedor del servicio a depurar (`docker stop m4-tramites`).
2. En Eclipse: *Run As → Spring Boot App* con el perfil activo `local` (`-Dspring.profiles.active=local`).
