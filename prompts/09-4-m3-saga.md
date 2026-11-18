# Prompt 09-4 — M3: saga orquestada y andamiaje de A3

> Rama `main`. Referencias: contrato (secciones 6.2, 6.3, 8), reglas 01–06. Antes de crear o modificar archivos, genera un Implementation Plan y espera mi aprobación.

## Objetivo didáctico

El flujo solicitud → pago → emisión coordinado por una saga orquestada (contenido 3.3), observable desde el frontend, la base de datos y Eclipse, con el punto de extensión de la asignación A3.

## Alcance (tramites-service, paquete `application.saga`)

1. `application/saga/SagaSolicitudTramite.java` implementa el flujo del contrato 6.2:
   - `POST /api/tramites` → `PENDIENTE_PAGO`.
   - `pago.confirmado` → `PAGADO` y envía el comando `emision.solicitar` (por Outbox) → `EN_EMISION`.
   - `emision.completada` → `EMITIDO`.
   - Estado de cada saga en `tramites.saga_instancia(tramite_id uuid pk, paso varchar(40), estado varchar(20), actualizado_en timestamptz)`.
   - Manejadores idempotentes (un evento repetido no avanza dos veces).
   - Javadoc didáctico: qué es una saga orquestada y dónde está cada paso; útil para poner puntos de parada en Eclipse.
2. **Andamiaje A3** (contrato, sección 8; regla 06):
   - `EstadoTramite.RECHAZADO` y la columna `motivo_rechazo` en la vista (ya creada en 09-2) proyectada desde `TramiteRechazado`.
   - Método `alFallarEmision(EmisionFallida evento)` en `SagaSolicitudTramite` con este cuerpo y nada más:
     ```java
     // TODO Asignación A3 – Tarea 2: compensar la saga.
     // 1) Solicitar la anulación del pago (POST /api/pagos/{id}/anulacion, ya implementado en pagos-service).
     // 2) Registrar TramiteRechazado con el motivo recibido (estado RECHAZADO y motivo_rechazo).
     log.warn("Sin compensación: ver asignación A3. Trámite {} queda EN_EMISION", evento.tramiteId());
     ```
   - Puerto de salida ya disponible para la anulación (`application/port/out/AnulacionPagoPort.java` con su adaptador HTTP), sin invocarse desde la saga.
   - `src/test/java/.../saga/SagaCompensacionTest.java` con `@Disabled("Asignación A3")`: con falla de emisión, el pago queda `ANULADO` y el trámite `RECHAZADO` con motivo.
3. **Frontend** `MODULO=m3`: iniciar trámite, pagar (genera la `Idempotency-Key` en el cliente), consultar con sondeo cada 2 s mostrando los estados intermedios, y ver el historial de eventos.
4. `docs/pruebas/colecciones/m3.postman_collection.json` con el flujo completo, el pago repetido y el historial.

## Pruebas

- Integración (Testcontainers PostgreSQL + RabbitMQ): camino feliz hasta `EMITIDO`.
- Con `EMISION_SIMULAR_FALLA=true`: el trámite queda `EN_EMISION` y existe el log de advertencia de A3.

## Criterios de aceptación

- Desde el frontend: iniciar → pagar → `EMITIDO` en pocos segundos, mostrando los pasos.
- `docker stop m3-pagos` → el trámite queda `PENDIENTE_PAGO`; al reiniciar y pagar, la saga continúa.
- Con la falla de emisión, el log muestra `Sin compensación: ver asignación A3`.
- `SagaCompensacionTest` aparece omitida; `mvn -f m3-datos verify` pasa.

## Commit

`feat(m3): saga orquestada solicitud-pago-emisión y andamiaje de A3`
