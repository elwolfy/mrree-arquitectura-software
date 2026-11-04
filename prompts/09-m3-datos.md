# Prompt 09 — M3: Persistencia y gestión de datos

> Rama `main`. Referencias: contrato (secciones 6.2, 6.3, 8), reglas 01–06, plan sección 7 (3.1–3.4) y sección 8 (S4, S5).

## Objetivo didáctico

Evolucionar M2 a una plataforma con el flujo completo **solicitud → pago → emisión**, coordinado por una Saga orquestada, con Outbox, idempotencia, Event Sourcing + CQRS para el trámite, enfoque documental con `JSONB` y material para optimización de consultas.

`m3-datos/` es **autosuficiente**: copia los servicios de M2 y los evoluciona (no referencia carpetas de M2). Compose `name: m3`.

## Cambios por servicio

### tramites-service

- **Event Sourcing:** el agregado `Tramite` se reconstruye desde `tramites.eventos_tramite` (solo inserciones, control de concurrencia optimista por `version`). Eventos: `TramiteRegistrado`, `PagoAsociado`, `EmisionSolicitada`, `TramiteEmitido`, `EmisionConError`.
- **CQRS:** `ProyectorVistaTramite` actualiza `tramites.vista_tramite` al guardar eventos. `GET /api/tramites/{id}` y `GET /api/tramites?dni=` leen **solo** de la vista. `GET /api/tramites/{id}/historial` lee los eventos. Endpoint de administración `POST /admin/proyecciones/vista-tramite/reconstruir` (borra y reconstruye la vista desde los eventos) — usado en S5.
- **Expediente documental:** `tramites.expediente.metadatos JSONB` (datos variables por tipo: país de destino, número de folios, idioma del documento…). Índice GIN. Endpoint `GET /api/tramites/buscar?pais=ES` que consulta sobre JSONB.
- **Saga orquestada** (`SagaSolicitudTramite`, en `application.saga`): implementa el flujo del contrato 6.2. Estado de la saga persistido en `tramites.saga_instancia(tramite_id, paso, estado, actualizado_en)`. Manejador de `emision.fallida` que en `main` **solo registra el error y deja el trámite en `EN_EMISION`** con un log `WARN` claro ("Sin compensación: ver asignación A3"). Javadoc que explique el punto de extensión.
- **Outbox:** los eventos de integración se escriben en `tramites.outbox` en la misma transacción; `RelayOutbox` (`@Scheduled`, cada 500 ms) publica y marca `publicado_en`. Documentar la alternativa CDC (Debezium) solo en el README.
- **Redis:** dependencia `spring-boot-starter-data-redis` incluida pero **sin uso** (punto de partida de A4). Propiedades de conexión configuradas.

### pagos-service

- `POST /api/pagos` con `Idempotency-Key` obligatorio (`400` si falta). Si la clave existe, devuelve el mismo pago (`200`) sin duplicar. Tabla `pagos.pago` del contrato.
- Publica `pago.confirmado` vía Outbox propio. Consume nada en `main` (la anulación es A3) — pero el endpoint interno `POST /api/pagos/{id}/anulacion` **no** existe en `main`.
- Tarifas como en M2.
- Propiedad `latencia.inyectada.ms` (por defecto 0) para M4.

### emision-service (nuevo, 8087)

- Consume el comando `emision.solicitar`, simula generar el documento (500 ms), publica `emision.completada` con `numeroDocumento` (`DOC-<año>-<secuencia>`) o `emision.fallida` si `emision.simular-falla=true`.
- Esquema `emision`.

### notificaciones-service

- Además consume `pago.confirmado` y `emision.completada`.

### Frontend

`MODULO=m3`, todas las funcionalidades activas: pago simulado con `Idempotency-Key` y consulta con sondeo e historial de eventos.

## Material de optimización (3.4)

- `docs/pruebas/sql/carga-masiva.sql`: genera 1 000 000 de registros en `tramites.vista_tramite` (y eventos mínimos) con `generate_series`, distribución realista de estados y fechas de los últimos 24 meses. Tiempo estimado documentado.
- `docs/pruebas/sql/consultas-optimizacion.sql`: consulta de estado por DNI **sin índice** (el índice `idx_vista_tramite_dni` se crea en el ejercicio) con `EXPLAIN ANALYZE`; paginación con `OFFSET 900000` vs. paginación por clave (keyset); ejemplo de particionamiento declarativo por rango de fecha en una tabla de ejemplo `tramites.vista_tramite_particionada`.
- Script `scripts/cargar-datos-masivos.(ps1|sh)` que ejecuta el SQL con `docker exec m3-postgres psql …`.
- Importante: la tabla `vista_tramite` en `main` **no** tiene índice por `dni` (es parte del ejercicio); sí por `tramite_id` (PK). Comentar en la migración.

## Pruebas

- Unitarias del agregado `Tramite` (reconstrucción desde eventos, transiciones inválidas → excepción).
- Saga: prueba de integración con Testcontainers (PostgreSQL + RabbitMQ) del camino feliz completo; prueba que con `emision.simular-falla=true` el trámite queda `EN_EMISION` y existe el log de advertencia.
- Idempotencia de pagos: dos `POST` con la misma clave → un solo registro.
- Outbox: si RabbitMQ no está disponible, el evento queda en `outbox` y se publica al recuperarse.
- Reconstrucción de la proyección.

## README de `m3-datos`

Diagrama de secuencia (Mermaid) de la saga, tabla de estados, experimentos S4 (flujo completo, detener pagos a mitad, reenviar pago con la misma clave, depurar el orquestador en Eclipse con puntos de parada) y S5 (historial, reconstruir proyección, JSONB, carga masiva y `EXPLAIN ANALYZE`), puntos de extensión de A3 y A4, memoria medida.

## Criterios de aceptación

- `/verificar-modulo m3-datos` pasa.
- Flujo desde el frontend: iniciar trámite → pagar → el estado avanza a `EMITIDO` en pocos segundos mostrando los pasos intermedios.
- Con `EMISION_SIMULAR_FALLA=true` el trámite queda `EN_EMISION` y el log muestra la advertencia de A3.
- Mismo `Idempotency-Key` dos veces → un pago.
- `scripts/cargar-datos-masivos` completa y `consultas-optimizacion.sql` muestra la diferencia antes/después del índice.
- No existen `motivo_rechazo`, `pago.anulado` publicado por pagos, ni caché Redis en uso (A3, A4).

## Commit

`feat(m3): persistencia y datos`
