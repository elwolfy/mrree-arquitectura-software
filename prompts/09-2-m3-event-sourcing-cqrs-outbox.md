# Prompt 09-2 — M3: Event Sourcing, CQRS y Outbox en `tramites-service`

> Rama `main`. Referencias: contrato (secciones 6.1–6.3), reglas 01–04. Antes de crear o modificar archivos, genera un Implementation Plan y espera mi aprobación. Solo modifica `m3-datos/tramites-service` (y su migración).

## Objetivo didáctico

Implementar el contenido 3.2 (escritura por eventos, lectura por vista) y el Outbox transaccional del contenido 3.3, con nombres y rutas que la presentación cita.

## Alcance (paquete `pe.utec.consular.tramites`)

1. **Event Sourcing** — `domain/Tramite.java`:
   - Agregado reconstruido desde `tramites.eventos_tramite(id bigserial pk, tramite_id uuid, version int, tipo varchar(40), datos jsonb, ocurrido_en timestamptz, unique(tramite_id, version))`. **Solo inserciones**: sin `UPDATE` ni `DELETE` (comentario en la migración).
   - Eventos: `TramiteRegistrado`, `PagoAsociado`, `EmisionSolicitada`, `TramiteEmitido`, `EmisionConError`. Dejar definidos también `TramiteRechazado` y `PagoAnulacionSolicitada` (los usará la saga y A3).
   - Concurrencia optimista por `version`: un conflicto de versión lanza una excepción de dominio clara.
   - Transiciones inválidas → excepción de dominio. Javadoc didáctico (qué es Event Sourcing y por qué se aplica aquí).
2. **CQRS** — `application/proyeccion/ProyectorVistaTramite.java`:
   - Mantiene `tramites.vista_tramite(tramite_id uuid pk, codigo, dni, tipo_tramite, estado, monto, motivo_rechazo, actualizado_en)` en la misma transacción que guarda los eventos. La columna `motivo_rechazo` existe desde ya (andamiaje de A3; contrato 6.3).
   - **Sin índice por `dni`** (solo PK): comentario en la migración que indica que el índice se crea en el laboratorio de la sesión 5.
   - `GET /api/tramites/{id}` y `GET /api/tramites?dni=` leen **solo** de la vista. `GET /api/tramites/{id}/historial` devuelve los eventos en orden (tipo, versión, fecha, datos).
   - `POST /admin/proyecciones/vista-tramite/reconstruir`: borra la vista y la reconstruye desde los eventos; responde cuántos trámites reconstruyó.
3. **Outbox** — tabla `tramites.outbox(id uuid pk, agregado_id uuid, tipo varchar(60), carga jsonb, creado_en timestamptz, publicado_en timestamptz null)`:
   - Los eventos de integración (`tramite.registrado`, comandos de la saga en 09-4) se insertan en la misma transacción que los eventos de dominio.
   - `adapter/out/outbox/RelayOutbox.java` (`@Scheduled`, cada 500 ms) publica los pendientes en RabbitMQ y marca `publicado_en`. Javadoc que explique la escritura doble que evita y mencione la alternativa CDC (Debezium).

## Pruebas

- Unitarias del agregado: reconstrucción desde eventos, transición inválida, conflicto de versión.
- Integración: reconstruir la proyección produce la misma vista; con RabbitMQ detenido, el evento queda en `outbox` y se publica al recuperarse.

## Criterios de aceptación

- Registrar un trámite crea eventos en `eventos_tramite`, la fila en `vista_tramite` y un registro en `outbox` publicado en menos de 1 s.
- `GET /api/tramites/{id}/historial` muestra la secuencia; la reconstrucción devuelve el mismo estado.
- `mvn -f m3-datos verify` pasa.

## Commit

`feat(m3): event sourcing, proyección CQRS y outbox en tramites`
