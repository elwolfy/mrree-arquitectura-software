# Prompt 08-4 — M2: `notificaciones-service` (Observer distribuido, idempotencia, DLQ)

> Rama `main`. Referencias: contrato (sección 6.1), reglas 01–04. Antes de crear o modificar archivos, genera un Implementation Plan y espera mi aprobación.

## Objetivo didáctico

El patrón Observer (2.1) llevado entre servicios: notificaciones reacciona a los eventos de otros contextos sin que estos lo conozcan. Es el servicio que los participantes detienen en el laboratorio de S3 para ver los mensajes esperar en la cola (2.3), y el modelo que copian en la asignación A2.

## Alcance

`m2-integracion/notificaciones-service` (8084, contenedor `m2-notificaciones`, `pe.utec.consular.notificaciones`, hexagonal). Relación en el context map: **conformista** frente al lenguaje publicado de los eventos.

1. **Consumidores** en `adapter/in/messaging/`:
   - `CitaReservadaConsumer.java` → cola `notificaciones.cita.reservada`.
   - `TramiteRegistradoConsumer.java` → cola `notificaciones.tramite.registrado`. **Es el modelo de la asignación A2**: código limpio y comentado, con Javadoc que explique paso a paso idempotencia, reintentos y DLQ.
2. **Idempotencia**: tabla `mensaje_procesado(message_id uuid pk, tipo_evento varchar, procesado_en timestamptz)` en el esquema `notificaciones` (Flyway). El registro del `messageId` y el aviso se guardan en la misma transacción; un mensaje repetido se reconoce y se descarta con log `INFO`.
3. **Reintentos y DLQ**: 3 reintentos con espera exponencial; luego el mensaje va a `<cola>.dlq`. Variable didáctica `NOTIFICACIONES_FALLAR_SIEMPRE=false` para demostrar la DLQ.
4. **Envío**: aviso por correo a Mailpit (`spring.mail`), con plantilla simple en español; registro del aviso en la tabla `aviso`.
5. **Consulta**: `GET /api/avisos?dni=` devuelve los avisos del ciudadano.

## Pruebas

- Integración (Testcontainers RabbitMQ + PostgreSQL + GreenMail o Mailpit): publicar dos veces el mismo `tramite.registrado` (mismo `messageId`) → un solo aviso y un solo correo.
- Con `NOTIFICACIONES_FALLAR_SIEMPRE=true` el mensaje termina en `notificaciones.tramite.registrado.dlq`.

## Criterios de aceptación

- `docker stop m2-notificaciones`; registrar tres trámites; la consola RabbitMQ muestra 3 mensajes en `notificaciones.tramite.registrado`; `docker start m2-notificaciones` → 3 correos en Mailpit (`localhost:8025`) y la cola en 0.
- No existe consumidor de `tramite.observado` en `main`.

## Commit

`feat(m2): notificaciones-service con consumidores idempotentes y DLQ`
