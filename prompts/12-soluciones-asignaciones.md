# Prompt 12 — Ramas de solución de las asignaciones

> Ejecutar una vez por asignación, cada una en una conversación nueva. Usar el workflow `/crear-solucion-asignacion`.

## Instrucción general

Ejecuta `/crear-solucion-asignacion <AN>`. La solución será publicada a los participantes como referencia: debe ser **mínima, correcta y ejemplar**, siguiendo exactamente los patrones del código existente. No refactorices nada fuera del alcance.

## A1 — `solucion/a1` (m1-estilos/monolito-modular)

- Caso de uso `ReprogramarCitaUseCase` + `ReprogramarCitaCommand`, servicio `ReprogramarCitaService`, `PUT /api/citas/{id}/reprogramacion` (`{fecha, hora}`), regla: solo si faltan ≥ 24 h para la cita actual (`422` `reprogramacion-fuera-de-plazo`), libera el cupo anterior y toma el nuevo con bloqueo pesimista (operación **CP**), estado `REPROGRAMADA`, notifica.
- Pruebas: unitaria de la regla de 24 h; integración feliz y fuera de plazo; ArchUnit verde.
- `docs/adr/ADR-001-estilo-y-consistencia.md` completo: estilo elegido para la plataforma (monolito modular evolutivo hacia microservicios por capacidad, justificado con los escenarios de calidad) y tabla CAP por operación, incluyendo la reprogramación.
- Frontend: botón "Reprogramar" en la consulta de citas (único cambio de frontend permitido en soluciones; justificar).

## A2 — `solucion/a2` (m2-integracion)

- tramites-service: `PUT /api/tramites/{id}/observacion` (`{motivo}`) → estado `OBSERVADO`, publica `tramite.observado` (`{tramiteId, codigo, dni, email, motivo}`).
- notificaciones-service: consumidor idempotente `notificaciones.tramite.observado` con DLQ; envía aviso.
- `GET /api/tramites/observados` en tramites + ruta en el gateway.
- `docs/contratos/asyncapi.yaml` actualizado.
- Pruebas: integración con RabbitMQ (evento publicado y consumido una vez).

## A3 — `solucion/a3` (m3-datos)

- Saga: ante `emision.fallida` → comando de anulación a pagos (`POST /api/pagos/{id}/anulacion` interno o comando por cola `pagos.comandos`, coherente con el estilo de la saga) → pagos publica `pago.anulado` → tramites registra `TramiteRechazado` con motivo → vista `RECHAZADO` + `motivo_rechazo`.
- Migración Flyway nueva `V<n>__motivo_rechazo.sql`.
- Eliminar el log "Sin compensación: ver asignación A3".
- Pruebas: integración de la saga con `emision.simular-falla=true` → pago `ANULADO`, trámite `RECHAZADO` con motivo; idempotencia de la compensación.

## A4 — `solucion/a4` (m3-datos)

- Puerto `CacheConsultaTramitePort` + adaptador Redis (cache-aside, TTL 60 s, clave `tramite:<id>`) en la consulta `GET /api/tramites/{id}`; invalidación en el proyector cuando cambia el estado.
- Cabecera de respuesta `X-Cache: HIT|MISS` (didáctica).
- `docs/c4/workspace.dsl` con Redis en la vista de contenedores (en m3/m4 según corresponda).
- `docs/asignaciones/A4-solucion.md` con mediciones de ejemplo de tiempo con y sin caché (medidas reales en la verificación, no inventadas).
- Pruebas: segunda consulta `HIT`; tras cambio de estado, `MISS` y dato actualizado (Testcontainers Redis).

## Criterios de aceptación (cada rama)

- CI verde.
- `git diff --stat main...solucion/<an>` muestra solo archivos del alcance.
- `docs/asignaciones/<AN>-solucion.md` con "Cómo verificar" ejecutado y su salida.
