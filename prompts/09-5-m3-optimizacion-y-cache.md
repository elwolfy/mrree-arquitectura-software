# Prompt 09-5 — M3: optimización de consultas, andamiaje de caché (A4) y README

> Rama `main`. Referencias: contrato (secciones 6.3, 8), reglas 01–06. Antes de crear o modificar archivos, genera un Implementation Plan y espera mi aprobación.

## Alcance

1. **Material de optimización** (contenido 3.4, laboratorio de la sesión 5):
   - `docs/pruebas/sql/carga-masiva.sql`: 1 000 000 de filas en `tramites.vista_tramite` (y eventos mínimos) con `generate_series`, distribución realista de estados, DNI y fechas de los últimos 24 meses. Incluye el DNI `40000001` con al menos 30 trámites. Tiempo estimado documentado.
   - `docs/pruebas/sql/consultas-optimizacion.sql`, en este orden y con comentarios:
     1. `EXPLAIN ANALYZE` de la consulta por DNI de la presentación (sin índice → `Seq Scan`).
     2. `CREATE INDEX idx_vista_tramite_dni ON tramites.vista_tramite(dni);` y repetición (→ `Index Scan`).
     3. Paginación con `OFFSET 900000` vs. paginación por clave `WHERE (actualizado_en, tramite_id) < (…) ORDER BY actualizado_en DESC, tramite_id DESC LIMIT 20`, con el índice compuesto necesario.
     4. Ejemplo de particionamiento declarativo por rango de fecha en `tramites.vista_tramite_particionada`.
     5. `DROP INDEX` para dejar el ejercicio repetible.
   - `scripts/cargar-datos-masivos.ps1` y `.sh` (ejecutan el SQL con `docker exec m3-postgres psql …`, muestran el avance y el tiempo total).
   - `docs/matrices/m3-optimizacion.md`: tabla vacía para registrar tiempos (sin índice, con índice, OFFSET, keyset) y conclusiones.
2. **Andamiaje A4** (contrato, sección 8; regla 06), en tramites-service:
   - `application/port/out/CacheConsultaTramitePort.java` (`obtener(id)`, `guardar(id, vista)`, `invalidar(id)`).
   - `adapter/out/cache/CacheNulaConsultaTramiteAdapter.java`, activo por defecto (no cachea).
   - La consulta `GET /api/tramites/{id}` usa el puerto (cache-aside) y agrega la cabecera `X-Cache: MISS`; `ProyectorVistaTramite` llama a `invalidar(id)` al cambiar el estado.
   - Propiedad `cache.consulta-tramite.tipo=nula|redis` y conexión a `m3-redis` configurada. **Sin** `RedisCacheConsultaTramiteAdapter` (tarea práctica de A4).
   - `src/test/java/.../cache/RedisCacheConsultaTramiteAdapterTest.java` con `@Disabled("Asignación A4")` (segunda lectura `X-Cache: HIT`; tras un cambio de estado, `MISS`).
3. **README de `m3-datos`**: diagrama de secuencia Mermaid de la saga, tabla de estados, URLs, experimentos de la sesión 4 (esquemas y JSONB; flujo completo; detener pagos; pagar dos veces; falla de emisión; depurar el orquestador en Eclipse con perfil `local`) y de la sesión 5 (historial; reconstruir la vista; carga masiva y `EXPLAIN ANALYZE`; OFFSET vs. keyset), puntos de extensión de A3 y A4, memoria medida y solución de problemas.

## Criterios de aceptación

- `scripts\cargar-datos-masivos.ps1` completa; `consultas-optimizacion.sql` muestra `Seq Scan` antes y `Index Scan` después del índice, y la diferencia de tiempos entre OFFSET y keyset.
- `GET /api/tramites/{id}` responde con `X-Cache: MISS`; no existe `RedisCacheConsultaTramiteAdapter`; la prueba de A4 aparece omitida.
- `mvn -f m3-datos verify` pasa.

## Commit

`feat(m3): material de optimización, andamiaje de caché y README`
