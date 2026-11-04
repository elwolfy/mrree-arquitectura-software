---
description: Compila, prueba, levanta y verifica una carpeta de módulo del curso. Uso: /verificar-modulo <m1-estilos|m2-integracion|m3-datos|m4-observabilidad> [perfil]
---

# /verificar-modulo

1. Leer `@docs/especificacion/contrato-global.md` y el README de la carpeta indicada.
2. Ejecutar las pruebas en contenedor desde la carpeta del módulo:
   `docker run --rm -v "<ruta>":/w -w /w maven:3.9-eclipse-temurin-21 mvn -B verify`
   Reportar número de pruebas ejecutadas, fallidas y omitidas. Si alguna falla, detenerse y corregir.
3. Ejecutar `docker compose down -v` y luego `docker compose [--profile <perfil>] up -d --build`.
4. Esperar hasta que `docker compose ps` muestre todos los servicios `healthy` (máximo 5 minutos). Si alguno no llega, mostrar sus últimos 50 logs y corregir.
5. Ejecutar las verificaciones funcionales del README del módulo (sección "Verificación rápida") y mostrar la salida.
6. Abrir `http://localhost:4200` en el navegador del agente y recorrer el flujo principal del módulo; adjuntar capturas.
7. Registrar `docker stats --no-stream` y comparar con el presupuesto de memoria de la regla 03.
8. Confirmar con `git grep` que no existen en `main` rutas, eventos o clases de las asignaciones (`reprogramacion`, `tramite.observado`, `motivo_rechazo`, `@Cacheable` en tramites).
9. Resumir en el Walkthrough: resultado de cada paso, evidencia y pendientes.
