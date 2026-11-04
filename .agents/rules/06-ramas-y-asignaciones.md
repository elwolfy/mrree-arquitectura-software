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
2. **No** implementar en `main` nada listado en la sección 8 del contrato (reprogramación, `tramite.observado`, compensación de la saga, caché Redis de consulta de trámite). Si una prueba, DTO, enumeración o ruta de esas funcionalidades aparece en `main`, es un error.
   - Excepción: los valores de enumeración `REPROGRAMADA`, `OBSERVADO`, `RECHAZADO` y `ANULADO` pueden existir en `main` solo si el contrato los requiere para otra funcionalidad; si no, se agregan en la rama de solución.
3. `main` debe tener **puntos de extensión claros** para que la asignación sea realizable en 30–60 minutos:
   - A1: el puerto, servicio y controlador de citas siguen un patrón que el participante puede copiar; `docs/asignaciones/A1.md` indica los archivos a crear.
   - A2: existe el publicador de eventos y un consumidor de ejemplo (`tramite.registrado`) que sirve de modelo.
   - A3: el orquestador de la saga tiene el manejador de `emision.fallida` que hoy solo registra el error.
   - A4: Redis ya está en el compose de m3 y la dependencia `spring-boot-starter-data-redis` está en el POM de tramites, sin usar.
4. Las ramas de solución incluyen: código, pruebas que validan los criterios de aceptación, actualización de documentación (ADR o C4 cuando corresponda) y una sección "Cómo verificar" en `docs/asignaciones/AN-solucion.md`.
5. Las ramas de solución deben pasar CI.

## Guías de asignación

Cada `docs/asignaciones/AN.md` contiene:

1. Objetivo y concepto del curso que practica (con sección del plan).
2. Punto de partida: tag y carpeta.
3. Pasos sugeridos (sin revelar el código completo).
4. Cómo ejecutar en **modo híbrido** desde Eclipse (perfil `local`).
5. Criterios de aceptación verificables (comandos curl o pasos en el frontend).
6. Cómo entregar: rama `aN-<usuario>` en su fork + capturas + enlace.

## Convención de commits

Conventional Commits en español: `feat(m1): …`, `fix(m2): …`, `docs: …`, `test(m3): …`, `chore: …`.
