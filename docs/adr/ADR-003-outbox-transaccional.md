# ADR-003: Prevención de escritura doble (Dual Write) mediante el patrón Transactional Outbox

- **Estado:** Aceptado
- **Fecha:** 2026-11-18
- **Módulo / Contexto:** M3 – Gestión de Datos (`m3-datos`) y M4 (`m4-observabilidad`) / `tramites-service` y `pagos-service`

---

## 1. Contexto

Al registrar un trámite (`POST /api/tramites`), avanzar un paso de la Saga (`emision.solicitar`) o confirmar/anular un pago (`POST /api/pagos`), el microservicio correspondiente debe realizar dos acciones:
1. Confirmar el cambio de estado en su esquema de **PostgreSQL** (`tramites` o `pagos`).
2. Publicar el evento o comando de integración en **RabbitMQ** (`consular.eventos`).

Si el servicio realiza el `COMMIT` en PostgreSQL e intenta publicar directamente en RabbitMQ dentro del método HTTP (escritura doble o *Dual Write*):
- Si RabbitMQ está temporalmente caído o la red falla tras el `COMMIT`, el trámite o el pago quedan guardados en PostgreSQL pero el mensaje jamás se publica, dejando la Saga permanentemente detenida.
- Si se publica en RabbitMQ antes del `COMMIT` y luego la transacción de base de datos hace `ROLLBACK`, los consumidores procesan un evento fantasma de un pago o trámite que no existe.

---

## 2. Decisión

Adoptamos el patrón **Transactional Outbox** con relay por sondeo periódico (`RelayOutbox`) tanto en `tramites-service` (`tramites.outbox`) como en `pagos-service` (`pagos.outbox`):

1. **Escritura atómica en una sola transacción ACID local:** El caso de uso (`RegistrarTramiteService`, `SagaSolicitudTramite`, `RegistrarPagoService`, `AnularPagoService`) inserta el mensaje serializado en JSONB dentro de la tabla `outbox` (`publicado_en = NULL`) en la **misma transacción de base de datos** que persiste los cambios de dominio.
2. **Relay asíncrono con garantía *at-least-once*:** El componente `RelayOutbox` (`@Scheduled(fixedDelay = 500)`) selecciona las filas con `publicado_en IS NULL ORDER BY creado_en ASC` (respaldado por un índice parcial `WHERE publicado_en IS NULL`), las publica en RabbitMQ usando el `id` de outbox como `messageId` (y propagando la cabecera `traceparent` en M4) y marca `publicado_en = NOW()`.
3. **Deduplicación en consumidores:** Como el relay garantiza entrega *al menos una vez*, todos los consumidores verifican el `messageId` en su tabla `mensaje_procesado` (o validan el estado actual del agregado en la Saga) para lograr procesamiento efectivamente único.

---

## 3. Alternativas Consideradas

| Alternativa | Ventajas | Motivo de descarte |
| --- | --- | --- |
| **1. Transactional Outbox con Relay por Polling `@Scheduled` (Elegida)** | No requiere infraestructura adicional fuera de PostgreSQL y Spring Boot; fácil de depurar en clase deteniendo `m3-rabbitmq` y observando las filas con `publicado_en IS NULL`. | Añade una consulta ligera cada 500 ms sobre el índice parcial `idx_outbox_pendientes`. |
| **2. Transactional Outbox con Change Data Capture (CDC / Debezium + Kafka Connect)** | Lee directamente el WAL (*Write-Ahead Log*) de PostgreSQL sin consultas `SELECT` de sondeo; ideal para caudales de miles de eventos por segundo. | Añade contenedores pesados (Kafka Connect / Debezium) que superarían el presupuesto de $\le 6\text{ GB}$ de RAM en las estaciones de laboratorio de los participantes. (Documentado en el Javadoc de `RelayOutbox` como alternativa industrial). |
| **3. Publicación directa a RabbitMQ tras el `@Transactional` (*Dual Write*)** | Sin tabla `outbox` ni proceso relay. | Pierde mensajes ante cualquier caída transitoria del broker o reinicio del contenedor entre el `COMMIT` y el `convertAndSend`. |

---

## 4. Consecuencias y Evidencia

- **Positivas:**
  - `POST /api/tramites` y `POST /api/pagos` responden `201/200` aun si RabbitMQ está detenido; en cuanto el broker se recupera, `RelayOutbox` drena los mensajes pendientes en menos de 1 segundo y la Saga continúa hasta `EMITIDO`.
- **Negativas / Riesgos aceptados:**
  - Latencia adicional de hasta `500 ms` por salto de outbox y necesidad de consumidores idempotentes.
- **Evidencia en el código:**
  - `tramites-service`: `V2__event_sourcing_cqrs_outbox.sql`, `OutboxPersistenceAdapter.java` y `RelayOutbox.java`.
  - `pagos-service`: `V1__esquema_pagos.sql`, `OutboxPersistenceAdapter.java` y `RelayOutbox.java`.

