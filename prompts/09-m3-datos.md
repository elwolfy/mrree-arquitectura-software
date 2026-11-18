# Prompt 09 — M3: Persistencia y gestión de datos (índice)

> **Este prompt no se ejecuta de una sola vez.** El Módulo 3 se genera en seis pasos, uno por conversación, en este orden. Cada paso deja el repositorio compilando, verificado y con su propio commit.

| Paso | Archivo | Resultado | Contenido del curso |
| --- | --- | --- | --- |
| 09-1 | `09-1-m3-base-y-expediente.md` | `m3-datos` autosuficiente (servicios de M2 copiados), esquemas y usuarios por servicio, scripts, expediente JSONB | 3.1 |
| 09-2 | `09-2-m3-event-sourcing-cqrs-outbox.md` | Trámite con Event Sourcing, proyección CQRS, historial, reconstrucción y Outbox | 3.2, 3.3 |
| 09-3 | `09-3-m3-pagos-emision.md` | Pagos con Idempotency-Key y Outbox, anulación (andamiaje A3), `emision-service`, avisos | 3.3 |
| 09-4 | `09-4-m3-saga.md` | Saga orquestada `SagaSolicitudTramite`, compensación con `TODO` (andamiaje A3), frontend m3 | 3.3 |
| 09-5 | `09-5-m3-optimizacion-y-cache.md` | Carga masiva, consultas de optimización, andamiaje de caché (A4), README | 3.4 |
| 09-6 | `09-6-m3-verificacion-y-a3.md` | Verificación integral, guía de A3 en `main` y solución en `solucion/a3`, etiqueta `s4` | Todos |

**Reglas comunes a los seis pasos:** antes de crear o modificar archivos, genera un *Implementation Plan* y espera mi aprobación. Respeta el contrato global (secciones 2, 3, 6, 6.1–6.3 y 8) y las reglas 01–06. No modifiques `m1-estilos` ni `m2-integracion`. Las rutas de clases y archivos que se indican aparecen tal cual en la presentación y el material de estudio del Módulo 3: no las renombres. Al terminar cada paso, deja los contenedores detenidos.

La guía y la solución de la asignación **A4** (caché + C4) se generan con el Módulo 4, porque su tarea conceptual usa `docs/c4/workspace.dsl`.
