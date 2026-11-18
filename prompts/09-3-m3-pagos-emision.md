# Prompt 09-3 — M3: pagos idempotentes, `emision-service` y avisos

> Rama `main`. Referencias: contrato (secciones 3, 6, 6.1, 6.3, 8), reglas 01–06. Antes de crear o modificar archivos, genera un Implementation Plan y espera mi aprobación.

## Alcance

1. **pagos-service** (`pe.utec.consular.pagos`):
   - `POST /api/pagos` con cabecera `Idempotency-Key` **obligatoria** (`400` `ProblemDetail` si falta). Si la clave ya existe, devuelve el mismo pago con `200` sin duplicar. Tabla `pagos.pago` del contrato con `unique(clave_idempotencia)`.
   - Lógica en `application/RegistrarPagoService.java` (ruta citada en la presentación), con Javadoc sobre idempotencia y referencia al Internet-Draft de la IETF.
   - Publica `pago.confirmado` mediante su **propio** Outbox (`pagos.outbox` + relay), sin compartir código con tramites.
   - **Andamiaje A3:** `POST /api/pagos/{id}/anulacion` (`{motivo}`) cambia el pago a `ANULADO` y publica `pago.anulado` vía Outbox. Completo y probado, pero **nadie lo invoca** en `main`.
   - Propiedad `latencia.inyectada.ms` (por defecto 0) para el Módulo 4.
   - Tarifas como en M2.
2. **emision-service** (nuevo, 8087, contenedor `m3-emision`, esquema `emision`):
   - Consume el comando `emision.solicitar` (cola `emision.comandos`), simula la generación del documento (500 ms) y publica `emision.completada` con `numeroDocumento` (`DOC-<año>-<secuencia de 6 dígitos>`) o `emision.fallida` con `motivo` si `EMISION_SIMULAR_FALLA=true`.
   - Consumidor idempotente (tabla `emision.mensaje_procesado`).
3. **notificaciones-service:** consume además `pago.confirmado` y `emision.completada` (avisos por Mailpit), con las mismas reglas de idempotencia y DLQ.
4. `rabbitmq/definitions.json` de M3: colas y `.dlq` de todos los eventos del contrato 6.1 que existen en M3 (sin `tramite.observado`) y la cola `emision.comandos`.
5. Script `m3-datos/scripts/pagar-dos-veces.ps1` y `.sh`: crea un trámite (o recibe su id), envía dos `POST /api/pagos` con la **misma** `Idempotency-Key`, muestra ambas respuestas y el conteo en `pagos.pago` para ese trámite (debe ser 1).

## Pruebas

- Idempotencia: dos `POST` con la misma clave → un registro y la misma respuesta; sin clave → `400`.
- Anulación: el pago pasa a `ANULADO` y se publica `pago.anulado`.
- emision-service: camino feliz y falla simulada.

## Criterios de aceptación

- `scripts\pagar-dos-veces.ps1` muestra un solo pago.
- `mvn -f m3-datos verify` pasa.

## Commit

`feat(m3): pagos idempotentes con outbox, emision-service y avisos`
