# Prompt 13 — Verificación integral, tags y preparación para publicación

> Rama `main`. Última conversación.

## Tareas

1. **Verificación integral** en una máquina limpia de Docker (`docker system prune -a` previo, con confirmación del usuario):
   - `/verificar-modulo m1-estilos` con cada perfil (`monolito`, `clean`, `microservicios`, `serverless`).
   - `/verificar-modulo m2-integracion`, `m3-datos`, `m4-observabilidad`.
   - Registrar tiempos del primer `up --build` y memoria por módulo en una tabla en `docs/instalacion/README.md`.
2. **Recorrido del participante (simulación):** seguir literalmente `docs/instalacion/04-fork-clonar-ejecutar.md` y `05-modo-hibrido-y-entrega.md` desde una carpeta nueva y reportar cualquier paso ambiguo o que falle; corregir las guías.
3. **Revisión de asignaciones en `main`:** el andamiaje de A1–A4 existe (contrato, sección 8), pero no su lógica central: `ReglaDeReprogramacion` lanza `UnsupportedOperationException`, no existe `TramiteObservadoConsumer`, el manejador de `emision.fallida` solo registra la advertencia, el adaptador de caché activo es el nulo, y las cuatro pruebas de asignación están con `@Disabled`.
4. **Coherencia con el plan del curso:** para cada fila de "Prueba" de la sección 7 del plan (1.1 a 4.4), confirmar que existe el código, script o archivo que la hace posible; generar `docs/trazabilidad-plan-codigo.md` (sección del plan → archivos/clases → comando de demostración).
5. **Tags:** crear `s1`…`s6` sobre el commit final de `main` (o sobre los commits correspondientes si el docente lo prefiere) con mensaje "Punto de partida sesión N".
6. **Publicación:** generar `docs/publicacion.md` con los pasos para el docente: crear el repositorio en GitHub, `git push --all` y `--tags`, reglas de protección de `main` y `solucion/*`, cuándo publicar cada rama de solución, plantilla del mensaje a los participantes.

## Criterios de aceptación

- Todas las verificaciones pasan; tabla de tiempos y memoria publicada.
- `docs/trazabilidad-plan-codigo.md` sin huecos (cada contenido 1.1–4.4 con al menos una evidencia ejecutable).
- Tags creados.

## Commit

`chore: verificación final`
