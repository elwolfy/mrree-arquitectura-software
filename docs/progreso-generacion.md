# Registro de Progreso de Generación — `mrree-arquitectura-software`

Registro automatizado del orquestador de generación por prompt.

| Archivo de Prompt | Estado | Commit | Fecha | Observaciones |
| --- | --- | --- | --- | --- |
| `prompts/09-1-m3-base-y-expediente.md` | completado | `7fc6b3a` | 2026-11-18 | Base autocontenida de `m3-datos/`, esquemas aislados (*Database-per-Service*) y `tramites.expediente` JSONB + índice GIN. |
| `prompts/09-2-m3-event-sourcing-cqrs-outbox.md` | completado | `2d7b111` | 2026-11-19 | Event Sourcing (`eventos_tramite`), CQRS (`ProyectorVistaTramite`, `vista_tramite` con `motivo_rechazo` y sin índice por `dni`) y Outbox transaccional (`RelayOutbox`). Nota: `contrato-global.md` 6.3 menciona `motivo_rechazo` en `solucion/a3`, pero los prompts `09-2`, `09-4` y `09-6` indican incluir la columna en la vista desde `09-2` como andamiaje para que `git diff main...solucion/a3` solo toque `SagaSolicitudTramite.java`, `SagaCompensacionTest.java`, `A3.md` y `A3-solucion.md`. |
| `prompts/09-3-m3-pagos-emision.md` | completado | `f8d1875` | 2026-11-19 | Pagos idempotentes con `Idempotency-Key` y Outbox propio (`RegistrarPagoService`, `AnularPagoService`), `emision-service` (`:8087`), consumidores en `notificaciones-service` y script `pagar-dos-veces.ps1`/`.sh` verificados. |
| `prompts/09-4-m3-saga.md` | completado | `989bd96` | 2026-11-19 | Saga orquestada `SagaSolicitudTramite`, tabla `tramites.saga_instancia`, andamiaje A3 (`AnulacionPagoPort`, `alFallarEmision` con TODO y `SagaCompensacionTest` `@Disabled`), frontend M3 y colección Postman `m3.postman_collection.json`. |
| `prompts/09-5-m3-optimizacion-y-cache.md` | completado | `2fb15bc` | 2026-11-19 | Carga masiva (1 000 000 filas en ~18.77 s), `consultas-optimizacion.sql` (`Seq Scan` 119.8 ms vs `Index Scan` 0.114 ms; `OFFSET 900000` 734.9 ms vs *Keyset* 0.154 ms), matriz `m3-optimizacion.md`, andamiaje A4 (`CacheConsultaTramitePort`, `CacheNulaConsultaTramiteAdapter`, `X-Cache: MISS`, `RedisCacheConsultaTramiteAdapterTest` `@Disabled`) y `m3-datos/README.md`. |
| `prompts/09-6-m3-verificacion-y-a3.md` | en curso | `-` | 2026-11-19 | Verificación integral de `m3-datos`, guía `docs/asignaciones/A3.md`, tag `s4` en `main` y solución de referencia en `solucion/a3`. |
