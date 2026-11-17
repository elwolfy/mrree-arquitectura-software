# Prompt 07-1 — M1: asignación A1 (andamiaje y guía en `main`, solución en `solucion/a1`)

> Ejecutar **después del prompt 07** y antes del 08-2. La guía se entrega al cierre de S2 (jue 26 nov) y la solución se revisa en S3 (vie 27 nov).
> Referencias: contrato (sección 8), reglas 02, 04 y 06, workflows `/verificar-modulo` y `/crear-solucion-asignacion`. Antes de crear o modificar archivos, genera un Implementation Plan y espera mi aprobación.

## Diseño de la asignación

Primera asignación del curso: debe ser **breve y alcanzable** (≈ 45 min) para que el participante termine con éxito. Dos tareas:

| Tarea | Qué hace el participante | Dónde |
| --- | --- | --- |
| 1 · Conceptual (≈ 20 min) | Decide si **reprogramar una cita** es una operación CP o AP y lo justifica con la matriz CAP de S2: decisión, una alternativa descartada y sus consecuencias | Sección "Consistencia de la reprogramación" de `docs/adr/ADR-001-estilo-y-consistencia.md` |
| 2 · Práctica (≈ 25 min) | Implementa la regla de negocio "solo se reprograma con 24 h o más de anticipación" en el dominio y habilita su prueba | `ReglaDeReprogramacion.java` y `ReglaDeReprogramacionTest.java` |

Todo lo demás (endpoint, caso de uso, servicio, bloqueo, estado) ya está hecho en `main`.

## Parte A — Rama `main`: andamiaje y guía

En `m1-estilos/monolito-modular`, módulo `citas`, siguiendo exactamente el patrón hexagonal del caso de uso de registrar cita:

1. `application/port/in/ReprogramarCitaUseCase.java` y `ReprogramarCitaCommand.java` (`citaId`, `fecha`, `hora`).
2. `application/service/ReprogramarCitaService.java` **completo**: en una transacción, carga la cita, invoca `ReglaDeReprogramacion.validar(citaActual, ahora)`, libera el cupo anterior y toma el nuevo con el **mismo método de bloqueo pesimista** que usa la reserva (operación CP), cambia el estado a `REPROGRAMADA` y notifica igual que la reserva. Javadoc que señale que la regla de 24 h es la tarea del participante.
3. `domain/ReglaDeReprogramacion.java`: clase de dominio sin Spring con el método `validar(Cita cita, LocalDateTime ahora)` cuyo cuerpo es:
   ```java
   // TODO Asignación A1 – Tarea 2: lanzar ReprogramacionFueraDePlazoException
   // si faltan menos de 24 horas para la fecha y hora actuales de la cita.
   throw new UnsupportedOperationException("Asignación A1 pendiente");
   ```
   y `domain/exception/ReprogramacionFueraDePlazoException.java` (ya creada).
4. `EstadoCita.REPROGRAMADA` y endpoint `PUT /api/citas/{id}/reprogramacion` (`{fecha, hora}`) en el controlador existente. El manejador global traduce `UnsupportedOperationException` a `501` (`ProblemDetail` "Funcionalidad pendiente: asignación A1") y `ReprogramacionFueraDePlazoException` a `422` con tipo `reprogramacion-fuera-de-plazo`.
5. `src/test/java/.../citas/domain/ReglaDeReprogramacionTest.java` con `@Disabled("Asignación A1")` y tres casos: faltan 48 h (válido), faltan 2 h (fuera de plazo), faltan exactamente 24 h (válido). Usa un `LocalDateTime` fijo, sin reloj del sistema.
6. `docs/adr/template.md` (plantilla resumida en español: título, estado, fecha, contexto, decisión, alternativas, consecuencias, evidencia).
7. `docs/adr/ADR-001-estilo-y-consistencia.md`:
   - Sección **"Estilo de la plataforma"** redactada completa como **ejemplo resuelto** (monolito modular evolutivo, sustentado en `docs/matrices/m1-estilos.md` y `m1-escenarios-calidad.md`).
   - Sección **"Consistencia de la reprogramación"** vacía, con la estructura a completar (decisión CP/AP, evidencia, alternativa descartada, consecuencias) y comentarios HTML con preguntas guía.
8. `docs/asignaciones/A1.md` con la estructura de la regla 06 (≈ 45 min; tarea 1 y tarea 2; rutas reales; cómo ejecutar la prueba en Eclipse y con `mvn -f m1-estilos -pl monolito-modular test -Dtest=ReglaDeReprogramacionTest`; verificación con Swagger: `200` con `REPROGRAMADA` a más de 24 h y `422` a menos de 24 h; entrega en la rama `a1-<usuario>` del fork; rúbrica 60 % conceptual / 40 % práctica). **Sin la solución.**
9. README de `m1-estilos`: sección "Asignación A1" con enlace a la guía.

Verificación en `main`: `mvn -B -f m1-estilos verify` pasa (la prueba de A1 aparece como omitida); `PUT /api/citas/{id}/reprogramacion` responde `501`; ArchUnit verde.

Commit en `main`: `feat(a1): andamiaje y guía de la asignación A1`. Etiqueta `s2`.

## Parte B — Rama `solucion/a1`

Ejecutar `/crear-solucion-asignacion A1`. La solución es **mínima y ejemplar**:

- Implementar `ReglaDeReprogramacion.validar` (comparación con `Duration.between`, sin dependencias de Spring) y quitar `@Disabled`.
- Completar en ADR-001 la sección "Consistencia de la reprogramación": **CP**, porque libera un cupo y ocupa otro con riesgo de asignación duplicada; alternativa AP descartada (reserva optimista con conciliación posterior) y consecuencias (posible `503` ante partición; menor disponibilidad aceptada).
- `docs/asignaciones/A1-solucion.md`: archivos modificados (deben ser solo los dos de la tarea 2 y el ADR), por qué la regla vive en el dominio, y "Cómo verificar" con la salida real de la prueba y de las dos llamadas a Swagger.

## Criterios de aceptación

- `main`: prueba de A1 omitida, endpoint responde `501`, `git grep -n "Duration.between" -- m1-estilos/monolito-modular/src/main/java/**/ReglaDeReprogramacion.java` sin resultados.
- `solucion/a1`: `mvn -B -f m1-estilos verify -pl monolito-modular` pasa con la prueba de A1 en verde; reprogramar a más de 24 h → `200` `REPROGRAMADA`; a menos de 24 h → `422`.
- `git diff --stat main...solucion/a1` muestra solo `ReglaDeReprogramacion.java`, `ReglaDeReprogramacionTest.java`, `ADR-001-estilo-y-consistencia.md` y `A1-solucion.md`.
- Al terminar, volver a `main` y dejar los contenedores detenidos.

## Commits

`main`: `feat(a1): andamiaje y guía de la asignación A1` · `solucion/a1`: `feat(m1): solución de referencia A1`
