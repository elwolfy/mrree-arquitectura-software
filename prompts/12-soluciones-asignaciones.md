# Prompt 12 — Ramas de solución de las asignaciones

> Ejecutar una vez por asignación, cada una en una conversación nueva. Usar el workflow `/crear-solucion-asignacion`.

## Instrucción general

Ejecuta `/crear-solucion-asignacion <AN>`. La solución será publicada a los participantes como referencia: debe ser **mínima, correcta y ejemplar**, siguiendo exactamente los patrones del código existente. No refactorices nada fuera del alcance.

## Modelo común

Cada asignación tiene una tarea conceptual y una tarea práctica (contrato, sección 8). La solución resuelve **solo** esas dos tareas sobre el andamiaje de `main`: el `git diff --stat` debe limitarse al archivo del `TODO`, su prueba habilitada, el documento conceptual y `docs/asignaciones/AN-solucion.md`.

## A1 — `solucion/a1`

Generada en el prompt 07-1. En este paso solo se verifica que siga vigente (rebase sobre `main` si corresponde).

## A2 — `solucion/a2`

Generada en el prompt 08-7. Solo verificar vigencia.

## A3 — `solucion/a3` (m3-datos)

- Conceptual: tabla de pasos y compensaciones de la saga completa en `docs/asignaciones/A3-solucion.md` (qué deshace cada paso, por qué orquestación y no coreografía).
- Práctica: manejador de `emision.fallida` en el orquestador → anulación en pagos (endpoint o comando ya existentes) → trámite `RECHAZADO` con motivo; quitar el log de advertencia y `@Disabled`.

## A4 — `solucion/a4` (m3-datos)

- Conceptual: Redis en la vista de contenedores de `docs/c4/workspace.dsl` y ADR breve sobre TTL e invalidación.
- Práctica: `RedisCacheConsultaTramiteAdapter` (cache-aside, TTL 60 s, clave `tramite:<id>`, cabecera `X-Cache: HIT|MISS`) activado en lugar del adaptador nulo; quitar `@Disabled`.

## Criterios de aceptación (cada rama)

- CI verde.
- `git diff --stat main...solucion/<an>` muestra solo archivos del alcance.
- `docs/asignaciones/<AN>-solucion.md` con "Cómo verificar" ejecutado y su salida.
