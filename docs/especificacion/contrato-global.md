# Contrato global del repositorio `mrree-arquitectura-software`

Este documento es la **fuente única de verdad** para nombres, puertos, rutas de API, esquemas de datos, eventos y datos semilla. Todos los prompts y reglas lo referencian. Cualquier cambio se hace primero aquí.

---

## 1. Dominio del caso integrador

**Plataforma Digital de Servicios Consulares y de Legalización.** Ciudadanos en el Perú y en el exterior reservan citas en oficinas consulares, inician trámites de legalización, apostilla y registro consular, pagan en línea y consultan el estado. La identidad se valida contra RENIEC mediante la PIDE (simulada).

> Todos los datos (oficinas, tarifas, DNI, personas) son **ficticios y de laboratorio**. No representan información real del MRREE.

### 1.1 Lenguaje ubicuo

| Término | Definición | Contexto |
| --- | --- | --- |
| Oficina consular | Sede que atiende citas en el exterior | Citas |
| Cupo | Franja horaria (fecha + hora) con capacidad en una oficina | Citas |
| Cita | Reserva de un cupo por un ciudadano para un tipo de trámite | Citas |
| Reprogramación | Cambio de cupo de una cita existente (asignación A1) | Citas |
| Solicitud | Pedido inicial del ciudadano para un trámite | Trámites |
| Trámite | Proceso con ciclo de vida (registrado → pagado → emitido) | Trámites |
| Expediente | Conjunto de datos y metadatos del trámite | Trámites |
| Observación | Estado de un trámite que requiere corrección del ciudadano | Trámites |
| Tarifa | Monto a pagar según tipo de trámite y país | Pagos / Tarifas |
| Orden de pago | Obligación de pago generada para un trámite | Pagos |
| Constancia | Comprobante de un pago confirmado | Pagos |
| Ciudadano | Persona identificada por DNI | Identidad |
| Validación de identidad | Verificación del DNI contra RENIEC vía PIDE | Identidad |
| Aviso | Notificación enviada al ciudadano | Notificaciones |

### 1.2 Enumeraciones

- `TipoTramite`: `LEGALIZACION`, `APOSTILLA`, `REGISTRO_CONSULAR`
- `EstadoCita`: `RESERVADA`, `REPROGRAMADA` (solo A1), `CANCELADA`, `ATENDIDA`
- `EstadoTramite`: `REGISTRADO`, `PENDIENTE_PAGO`, `PAGADO`, `EN_EMISION`, `EMITIDO`, `OBSERVADO` (A2), `RECHAZADO` (A3)
- `EstadoPago`: `PENDIENTE`, `CONFIRMADO`, `ANULADO`
- `EstadoNotificacion`: `ENVIADA`, `PENDIENTE`, `FALLIDA`
- `CanalNotificacion`: `CONSOLA`, `CORREO`
- `ModoConsistencia`: `CP`, `AP`

---

## 2. Convenciones técnicas

| Elemento | Valor |
| --- | --- |
| Paquete base Java | `pe.utec.consular` |
| Java | 21 (Eclipse Temurin) |
| Spring Boot | 4.0.x (último parche estable). Si una dependencia requerida no es compatible, usar 3.5.x en **todo** el repositorio y registrarlo en `docs/adr/ADR-000-version-de-plataforma.md` |
| Spring Cloud | Tren compatible con la versión de Spring Boot elegida (2025.1.x para Boot 4.0.x) |
| Spring Modulith | 2.0.x (Boot 4) / 1.4.x (Boot 3.5) |
| Build | Maven 3.9, un POM agregador por carpeta de módulo (`m1-estilos/pom.xml`, etc.) con `spring-boot-starter-parent` como padre |
| Base de datos | PostgreSQL 16 (imagen `postgres:16-alpine`), base `consular`, un esquema por servicio/módulo |
| Migraciones | Flyway, scripts en `src/main/resources/db/migration/<esquema>/V<n>__<descripcion>.sql` |
| Documentación API | springdoc-openapi, Swagger UI en `/swagger-ui.html`, contrato en `/v3/api-docs` |
| Errores HTTP | RFC 9457 Problem Details (`ProblemDetail`), `type` = `https://consular.lab/errores/<codigo>` |
| Fechas | ISO-8601; zona `America/Lima` en servidor; `LocalDate`/`LocalTime` en API |
| Frontend | Angular (última versión estable, componentes standalone) + PrimeNG; servido por `nginx:alpine` |
| Imágenes Java | Build: `maven:3.9-eclipse-temurin-21`; runtime: `eclipse-temurin:21-jre-alpine` |
| Usuario de BD por servicio | `<esquema>_app` / contraseña `<esquema>_pwd` (solo laboratorio) |

---

## 3. Mapa de puertos (host)

Solo se ejecuta **una carpeta de módulo a la vez**; por eso los puertos se reutilizan entre módulos.

| Componente | Puerto | Módulos |
| --- | --- | --- |
| Frontend (nginx) | 4200 | m1–m4 |
| Punto de entrada de API en M1 (monolito / citas-service / enrutador FaaS / clean) | 8081 | m1 |
| API Gateway | 8080 | m2–m4 |
| citas-service | 8081 | m2–m4 |
| tramites-service | 8082 (HTTP), 9082 (gRPC) | m2–m4 |
| pagos-service | 8083 | m2–m4 |
| notificaciones-service | 8084 | m1 (microservicios), m2–m4 |
| bff-web | 8085 | m2–m4 |
| identidad-service | 8086 | m2–m4 |
| emision-service | 8087 | m3–m4 |
| Simulador PIDE (WireMock) | 8089 | m2–m4 |
| Structurizr Lite | 8090 | m4 |
| PostgreSQL | 5432 | m1–m4 |
| RabbitMQ AMQP / consola | 5672 / 15672 (`consular` / `consular`) | m2–m4 |
| Redis | 6379 | m3–m4 |
| Mailpit (SMTP / web) | 1025 / 8025 | m1–m4 |
| Grafana (LGTM) | 3000 (`admin` / `admin`) | m4 |
| OTLP gRPC / HTTP | 4317 / 4318 | m4 |
| Envoy (sidecar) admin | 9901 | m2 (perfil `sidecar`) |

---

## 4. API de M1 (`m1-estilos`)

Todas las variantes de M1 (monolito, microservicios, serverless, clean) exponen **la misma API** en `http://localhost:8081`, para que el frontend y los scripts k6 funcionen sin cambios.

| Método | Ruta | Descripción | Respuesta |
| --- | --- | --- | --- |
| GET | `/api/oficinas` | Lista de oficinas consulares | `200` `[OficinaDto]` |
| GET | `/api/oficinas/{oficinaId}/disponibilidad?fecha=YYYY-MM-DD` | Cupos del día con capacidad libre | `200` `DisponibilidadDto` |
| POST | `/api/citas` | Reserva una cita | `201` `CitaDto` + header `Location` |
| GET | `/api/citas/{citaId}` | Consulta una cita | `200` `CitaDto` / `404` |
| GET | `/api/citas?dni=XXXXXXXX` | Citas de un ciudadano | `200` `[CitaDto]` |
| DELETE | `/api/citas/{citaId}` | Cancela una cita | `204` |
| GET | `/api/tarifas?tipoTramite=&pais=` | Tarifa calculada (patrón Strategy) — solo monolito y clean | `200` `TarifaDto` |
| GET | `/api/sistema/info` | Variante en ejecución y modo de consistencia | `200` `InfoSistemaDto` |
| PUT | `/api/citas/{citaId}/reprogramacion` | **Solo en `solucion/a1`** | `200` `CitaDto` |

### 4.1 DTO (JSON)

```json
// OficinaDto
{ "id": "MAD", "nombre": "Consulado General del Perú en Madrid (laboratorio)", "pais": "ES", "ciudad": "Madrid", "zonaHoraria": "Europe/Madrid" }

// DisponibilidadDto
{ "oficinaId": "MAD", "fecha": "2026-11-30",
  "cupos": [ { "hora": "08:00", "capacidad": 2, "ocupados": 1, "libres": 1 } ],
  "origen": "BASE_DE_DATOS", "actualizadoHaceSegundos": 0 }
// origen: BASE_DE_DATOS | CACHE (modo AP con partición)

// RegistrarCitaRequest
{ "dni": "40000001", "nombres": "Ana Pérez", "email": "ana@correo.lab", "oficinaId": "MAD",
  "fecha": "2026-11-30", "hora": "08:00", "tipoTramite": "LEGALIZACION" }

// CitaDto
{ "id": "6f1c…", "codigo": "CIT-2026-000123", "dni": "40000001", "nombres": "Ana Pérez",
  "oficinaId": "MAD", "fecha": "2026-11-30", "hora": "08:00", "tipoTramite": "LEGALIZACION",
  "estado": "RESERVADA", "notificacion": "ENVIADA", "creadaEn": "2026-11-24T19:05:00-05:00" }

// TarifaDto
{ "tipoTramite": "APOSTILLA", "pais": "ES", "moneda": "PEN", "monto": 30.00, "estrategia": "ApostillaTarifaStrategy" }

// InfoSistemaDto
{ "variante": "MONOLITO_MODULAR", "arquitecturaInterna": "HEXAGONAL", "modoConsistencia": "CP", "version": "1.0.0" }
// variante: MONOLITO_MODULAR | MICROSERVICIOS | SERVERLESS ; arquitecturaInterna: HEXAGONAL | CLEAN
```

### 4.2 Reglas de negocio de M1

1. Un cupo no puede superar su capacidad (no se asigna el mismo cupo dos veces por encima de la capacidad): bloqueo pesimista `SELECT … FOR UPDATE` sobre el cupo.
2. Un ciudadano no puede tener dos citas `RESERVADA` el mismo día en la misma oficina → `409` `cita-duplicada`.
3. DNI: 8 dígitos. Email válido. Fecha ≥ mañana y ≤ 30 días. Hora debe existir como cupo.
4. Al reservar, se notifica al ciudadano (canal `CONSOLA` por defecto; `CORREO` envía a Mailpit).
5. Código de cita: `CIT-<año>-<secuencia de 6 dígitos>`.
6. **Modo de consistencia** (`consistencia.modo`, por defecto `CP`), solo en monolito y clean:
   - Reserva (`POST /api/citas`): siempre CP. Si la base de datos no responde en 2 s → `503` `base-de-datos-no-disponible` con mensaje claro.
   - Disponibilidad (`GET …/disponibilidad`): en `CP` → `503` si la base no responde; en `AP` → responde desde caché en memoria (Caffeine, refresco cada 10 s) con `origen: CACHE` y `actualizadoHaceSegundos`.
7. Tarifas (valores ficticios de laboratorio, PEN): `LEGALIZACION` 20.00, `APOSTILLA` 30.00; recargo por país fuera de Sudamérica +10 %. `REGISTRO_CONSULAR` **no existe** en `main`: se agrega en clase (S2) como nueva estrategia.

### 4.3 Simulación de fallas en M1

| Propiedad | Efecto |
| --- | --- |
| `notificaciones.simular-falla=true` | El adaptador/servicio de notificaciones lanza una excepción |
| `notificaciones.canal=CONSOLA|CORREO` | Canal de envío |
| `consistencia.modo=CP|AP` | Modo de consistencia |

Comportamiento esperado ante falla de notificaciones (es la lección de la matriz de estilos):

- **Monolito:** la notificación se invoca de forma síncrona dentro de la transacción → la reserva **falla** (`500`) y se revierte. Se documenta en el código como acoplamiento deliberado para la comparación.
- **Microservicios:** citas-service llama a notificaciones-service por REST con timeout de 2 s y circuit breaker (Resilience4j) → la reserva **se confirma** con `notificacion: PENDIENTE`.
- **Serverless:** la función de reserva invoca la función de notificación de forma asíncrona → la reserva **se confirma** con `notificacion: PENDIENTE`.

---

## 5. Esquemas de base de datos de M1

Esquema `citas`:

```sql
oficina_consular(id varchar(5) pk, nombre varchar(120), pais char(2), ciudad varchar(60), zona_horaria varchar(40))
cupo(id uuid pk, oficina_id fk, fecha date, hora time, capacidad int, ocupados int, version int, unique(oficina_id, fecha, hora))
cita(id uuid pk, codigo varchar(20) unique, dni char(8), nombres varchar(120), email varchar(120),
     oficina_id fk, cupo_id fk, tipo_tramite varchar(20), estado varchar(20), notificacion varchar(20), creada_en timestamptz)
-- secuencia: cita_codigo_seq
```

Esquema `notificaciones`: `aviso(id uuid pk, destinatario varchar(120), asunto varchar(160), cuerpo text, canal varchar(10), estado varchar(10), creado_en timestamptz, referencia varchar(40))`

Esquema `tramites` (en M1 solo tarifas): `tarifa_base(tipo_tramite varchar(20) pk, monto numeric(10,2), moneda char(3))`

**Datos semilla (Flyway `R__` repetible o `V2__semilla`):** 6 oficinas — `MAD` Madrid (ES), `BUE` Buenos Aires (AR), `SCL` Santiago (CL), `MIA` Miami (US), `MIL` Milán (IT), `TYO` Tokio (JP). Cupos para los próximos 30 días hábiles, de 08:00 a 12:30 cada 30 minutos, capacidad 2. Se generan al iniciar si no existen (componente `GeneradorDeCupos`).

DNI de prueba: `40000001` a `40000050` (válidos en la PIDE simulada); `49999999` (fallecido → rechazo), `48888888` (no encontrado).

---

## 6. API de M2–M4 (a través del API Gateway `:8080`)

| Método | Ruta pública (gateway) | Servicio destino |
| --- | --- | --- |
| GET/POST/DELETE | `/api/citas/**`, `/api/oficinas/**` | citas-service |
| POST | `/api/tramites` | tramites-service (inicia trámite y saga en M3) |
| GET | `/api/tramites/{id}` | tramites-service (modelo de lectura en M3) |
| GET | `/api/tramites/{id}/historial` | tramites-service (M3, event sourcing) |
| GET | `/api/tramites?dni=` | tramites-service |
| POST | `/api/pagos` | pagos-service (header `Idempotency-Key` obligatorio) |
| GET | `/api/pagos/{id}` | pagos-service |
| GET | `/api/tarifas?tipoTramite=&pais=` | pagos-service |
| POST | `/api/identidad/validaciones` | identidad-service → PIDE simulada |
| GET | `/bff/inicio?dni=` | bff-web: agrega citas + trámites + avisos del ciudadano |
| POST | `/bff/tramites` | bff-web: fachada "iniciar trámite" (valida identidad + crea trámite + calcula tarifa) |
| GET | `/api/tramites/observados` | **Solo en `solucion/a2`** |

**gRPC (tramites-service :9082):** `consular.tramites.v1.TramitesService/ConsultarTramite(ConsultarTramiteRequest{id}) → TramiteResponse`. Archivo `tramites/src/main/proto/tramites.proto`.

### 6.1 Eventos (RabbitMQ, exchange `consular.eventos`, tipo `topic`)

| Routing key | Emisor | Consumidores | Carga útil |
| --- | --- | --- | --- |
| `cita.reservada` | citas | notificaciones | `{citaId, codigo, dni, email, oficinaId, fecha, hora}` |
| `tramite.registrado` | tramites | notificaciones | `{tramiteId, codigo, dni, email, tipoTramite}` |
| `pago.confirmado` | pagos | tramites (saga), notificaciones | `{pagoId, tramiteId, monto}` |
| `pago.anulado` | pagos | tramites, notificaciones | `{pagoId, tramiteId, motivo}` |
| `emision.completada` | emision | tramites | `{tramiteId, numeroDocumento}` |
| `emision.fallida` | emision | tramites (saga) | `{tramiteId, motivo}` |
| `tramite.observado` | tramites | notificaciones | **Solo en `solucion/a2`** |

Todos los mensajes llevan cabeceras `messageId` (UUID), `tipoEvento`, `ocurridoEn`, `traceparent` (M4). Cada consumidor tiene su cola `<servicio>.<evento>` y una cola de mensajes fallidos `<cola>.dlq`.

### 6.2 Saga (M3) — flujo solicitud → pago → emisión (orquestada en tramites-service)

1. `POST /api/tramites` → evento interno `TramiteRegistrado` → estado `PENDIENTE_PAGO`.
2. Ciudadano paga (`POST /api/pagos`) → `pago.confirmado` → tramites pasa a `PAGADO` y ordena la emisión (comando `emision.solicitar` vía cola `emision.comandos`) → `EN_EMISION`.
3. `emision.completada` → `EMITIDO`.
4. `emision.fallida` → en `main`: el trámite queda `EN_EMISION` con un registro de error (**sin compensación**). La compensación (anular pago + `RECHAZADO` + `motivo_rechazo`) es la asignación **A3**.

Falla simulada: `emision.simular-falla=true` en emision-service.

### 6.3 Esquemas M3 relevantes

- `tramites.eventos_tramite(id bigserial pk, tramite_id uuid, version int, tipo varchar(40), datos jsonb, ocurrido_en timestamptz, unique(tramite_id, version))` — solo inserciones.
- `tramites.vista_tramite(tramite_id uuid pk, codigo, dni, tipo_tramite, estado, monto, actualizado_en)` — proyección CQRS.
- `tramites.expediente(tramite_id uuid pk, metadatos jsonb)` — enfoque documental.
- `tramites.outbox(id uuid pk, agregado_id uuid, tipo varchar(60), carga jsonb, creado_en timestamptz, publicado_en timestamptz null)`.
- `pagos.pago(id uuid pk, tramite_id uuid, monto numeric, estado varchar(15), clave_idempotencia varchar(64) unique, creado_en timestamptz)`.
- Columna `motivo_rechazo` en `vista_tramite`: **solo en `solucion/a3`** (migración Flyway nueva).

---

## 7. Observabilidad (M4)

- Agente Java de OpenTelemetry en cada contenedor (`JAVA_TOOL_OPTIONS=-javaagent:/otel/opentelemetry-javaagent.jar`), exportando OTLP a `lgtm:4317`. `OTEL_SERVICE_NAME=<servicio>`.
- Logs JSON con `trace_id` y `span_id` (Logback + encoder JSON).
- Métricas de negocio con Micrometer: `consular_tramites_registrados_total{tipo}`, `consular_pagos_confirmados_total`, `consular_emision_duracion_seconds` (histograma).
- Latencia inyectada: `latencia.inyectada.ms` en pagos-service (por defecto 0; el escenario de clase usa 3000).
- Tableros de Grafana aprovisionados desde `m4-observabilidad/grafana/dashboards/*.json`: "Plataforma consular – visión general" y "Flujo de trámite".
- SLO de referencia: 99 % de consultas de estado de trámite con latencia < 500 ms.

---

## 8. Asignaciones (no deben existir en `main`)

| Asignación | Rama de solución | Alcance |
| --- | --- | --- |
| A1 | `solucion/a1` | `PUT /api/citas/{id}/reprogramacion` en `m1-estilos/monolito-modular` (hexagonal), regla ≥ 24 h, `EstadoCita.REPROGRAMADA`, prueba ArchUnit verde, `docs/adr/ADR-001-estilo-y-consistencia.md` completo |
| A2 | `solucion/a2` | Evento `tramite.observado` (tramites → notificaciones), `PUT /api/tramites/{id}/observacion` interno, ruta gateway `/api/tramites/observados` (m2) |
| A3 | `solucion/a3` | Compensación de la saga en m3: ante `emision.fallida` anular pago (`pago.anulado`), estado `RECHAZADO`, migración `V<n>__motivo_rechazo.sql` |
| A4 | `solucion/a4` | Caché Redis cache-aside (TTL 60 s) en `GET /api/tramites/{id}` en m3, invalidación al cambiar estado, Redis en el C4 de contenedores |

---

## 9. Etiquetas Git por sesión

| Tag | Contenido |
| --- | --- |
| `s1` | `main` con M1 completo + frontend |
| `s2` | Igual a `s1` (punto de partida de S2) |
| `s3` | `main` con M2 |
| `s4` | `main` con M3 |
| `s5` | `main` con M3 + C4 |
| `s6` | `main` con M4 |

En la práctica `main` contiene todo desde el inicio; los tags marcan el commit de referencia de cada sesión.
