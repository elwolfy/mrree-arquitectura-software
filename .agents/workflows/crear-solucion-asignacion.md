---
description: Crea la rama de solución de una asignación a partir de main. Uso: /crear-solucion-asignacion <A1|A2|A3|A4>
---

# /crear-solucion-asignacion

1. Leer la sección 8 de `@docs/especificacion/contrato-global.md`, `@.agents/rules/06-ramas-y-asignaciones.md` y `docs/asignaciones/<AN>.md`.
2. `git checkout main && git pull` y crear `solucion/<an>` (minúsculas).
3. Implementar **solo** el alcance de la asignación, siguiendo los mismos patrones del código existente (es la solución que verán los participantes: debe ser ejemplar y mínima).
4. Agregar pruebas que demuestren cada criterio de aceptación de la guía.
5. Actualizar documentación asociada (ADR-001 en A1; C4 en A4) y crear `docs/asignaciones/<AN>-solucion.md` con: archivos modificados, explicación del diseño y "Cómo verificar".
6. Ejecutar `/verificar-modulo` sobre la carpeta afectada y los comandos de "Cómo verificar".
7. Commit `feat(<modulo>): solución de referencia <AN>` y mostrar `git diff --stat main...solucion/<an>` en el Walkthrough.
