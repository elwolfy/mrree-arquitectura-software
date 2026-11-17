---
description: Gestión de ramas, asignaciones y puntos de extensión para los participantes
activation: always
---

# Ramas, asignaciones y puntos de extensión

## Modelo de ramas

| Rama / tag | Contenido | Quién la modifica |
| --- | --- | --- |
| `main` | Aplicación completa sin A1–A4. Protegida | Solo el docente vía pull request |
| `solucion/a1` … `solucion/a4` | `main` + la solución de la asignación (cada una parte de `main`, no se encadenan) | Docente |
| `demo/strangler` | Demostración opcional Strangler Fig (S6) | Docente |
| Tags `s1` … `s6` | Puntos de partida de cada sesión | Docente |
| Ramas `a1-<usuario>` … en el **fork** del participante | Trabajo del participante | Participante |

## Reglas para el agente

1. Trabajar en `main` salvo que el prompt indique otra rama.
2. Cada asignación tiene **dos tareas** (contrato, sección 8): una **conceptual** y una **práctica**. En `main` se implementa el **andamiaje** de la tarea práctica, pero **no** su lógica central (la regla de 24 h, el consumidor de `tramite.observado`, la compensación de la saga, el adaptador Redis) ni la respuesta de la tarea conceptual. Si esa lógica aparece en `main`, es un error.
3. El andamiaje deja la tarea práctica realizable en unos 25 minutos por un participante con conocimientos básicos de Java:
   - Firmas, cableado y endpoint ya existen; el punto a completar tiene un `TODO` explícito y responde `501` o registra una advertencia mientras no se implemente.
   - Existe una prueba con `@Disabled("Asignación AN")` que el participante habilita: es su criterio de aceptación.
   - Hay un ejemplo equivalente en el código que sirve de modelo (indicado con su ruta en la guía).
4. Las ramas de solución incluyen: código, pruebas que validan los criterios de aceptación, actualización de documentación (ADR o C4 cuando corresponda) y una sección "Cómo verificar" en `docs/asignaciones/AN-solucion.md`.
5. Las ramas de solución deben pasar CI.

## Guías de asignación

Cada `docs/asignaciones/AN.md` contiene:

1. Objetivo y concepto del curso que practica (con sección del plan). Tiempo estimado: ≈ 45 min.
2. **Tarea 1 – conceptual (≈ 20 min):** qué decidir, dónde escribirlo y qué evidencia usar.
3. **Tarea 2 – práctica (≈ 25 min):** archivo con el `TODO`, ejemplo modelo, prueba a habilitar.
4. Cómo ejecutar en **modo híbrido** desde Eclipse (perfil `local`).
5. Criterios de aceptación verificables de cada tarea (prueba en verde y un comando curl o paso en el frontend).
6. Cómo entregar: rama `aN-<usuario>` en su fork.
7. Rúbrica breve: tarea conceptual 60 %, tarea práctica 40 %.

## Convención de commits

Conventional Commits en español: `feat(m1): …`, `fix(m2): …`, `docs: …`, `test(m3): …`, `chore: …`.
