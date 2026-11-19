-- ==============================================================================
-- consultas-optimizacion.sql — Laboratorio de Optimización de Consultas (M3 - Sesión 5)
-- Precondición: haber ejecutado `cargar-datos-masivos.ps1` (1 000 000 de filas en tramites.vista_tramite).
-- ==============================================================================

\timing on

-- ------------------------------------------------------------------------------
-- PASO 1: EXPLAIN ANALYZE de la consulta por DNI SIN ÍNDICE (-> Parallel Seq Scan / Seq Scan)
-- ------------------------------------------------------------------------------
-- Como tramites.vista_tramite se creó deliberadamente sin índice sobre `dni`,
-- PostgreSQL debe recorrer secuencialmente el millón de filas para filtrar `dni = '40000001'`.
DROP INDEX IF EXISTS tramites.idx_vista_tramite_dni;
DROP INDEX IF EXISTS tramites.idx_vista_tramite_keyset;

EXPLAIN (ANALYZE, BUFFERS)
SELECT tramite_id, codigo, tipo_tramite, estado, monto, actualizado_en
FROM tramites.vista_tramite
WHERE dni = '40000001'
ORDER BY actualizado_en DESC;

-- ------------------------------------------------------------------------------
-- PASO 2: Creación del índice B-Tree sobre `dni` y repetición (-> Index Scan / Bitmap Index Scan)
-- ------------------------------------------------------------------------------
-- Al crear `idx_vista_tramite_dni`, el optimizador reemplaza el recorrido secuencial
-- por una búsqueda directa en el árbol B-Tree, reduciendo el tiempo de decenas de ms a < 1 ms.
CREATE INDEX idx_vista_tramite_dni ON tramites.vista_tramite(dni);
ANALYZE tramites.vista_tramite;

EXPLAIN (ANALYZE, BUFFERS)
SELECT tramite_id, codigo, tipo_tramite, estado, monto, actualizado_en
FROM tramites.vista_tramite
WHERE dni = '40000001'
ORDER BY actualizado_en DESC;

-- ------------------------------------------------------------------------------
-- PASO 3: Paginación profunda con OFFSET 900000 vs. Keyset Pagination (Seek Method)
-- ------------------------------------------------------------------------------
-- Creamos el índice compuesto necesario para ordenar y buscar por clave `(actualizado_en DESC, tramite_id DESC)`.
CREATE INDEX idx_vista_tramite_keyset
    ON tramites.vista_tramite (actualizado_en DESC, tramite_id DESC);
ANALYZE tramites.vista_tramite;

-- 3a. Paginación con OFFSET 900000: obliga a PostgreSQL a leer y descartar 900 000 filas antes de entregar 20.
EXPLAIN (ANALYZE, BUFFERS)
SELECT tramite_id, codigo, dni, estado, actualizado_en
FROM tramites.vista_tramite
ORDER BY actualizado_en DESC, tramite_id DESC
LIMIT 20 OFFSET 900000;

-- 3b. Paginación por clave (Keyset Pagination / Cursor): salta directamente al punto del índice B-Tree
--     y lee únicamente las 20 filas solicitadas en tiempo constante O(log N).
EXPLAIN (ANALYZE, BUFFERS)
SELECT tramite_id, codigo, dni, estado, actualizado_en
FROM tramites.vista_tramite
WHERE (actualizado_en, tramite_id) < ('2025-03-01T00:00:00Z'::timestamptz, 'ffffffff-ffff-ffff-ffff-ffffffffffff'::uuid)
ORDER BY actualizado_en DESC, tramite_id DESC
LIMIT 20;

-- ------------------------------------------------------------------------------
-- PASO 4: Ejemplo de Particionamiento Declarativo por Rango de Fecha (RANGE Partitioning)
-- ------------------------------------------------------------------------------
-- Demuestra cómo dividir físicamente una proyección histórica por año en PostgreSQL 16
-- para habilitar Partition Pruning cuando se filtra por rango de `actualizado_en`.
DROP TABLE IF EXISTS tramites.vista_tramite_particionada CASCADE;

CREATE TABLE tramites.vista_tramite_particionada (
    tramite_id     UUID NOT NULL,
    codigo         VARCHAR(20) NOT NULL,
    dni            CHAR(8) NOT NULL,
    tipo_tramite   VARCHAR(25) NOT NULL,
    estado         VARCHAR(20) NOT NULL,
    monto          NUMERIC(10, 2) NOT NULL,
    actualizado_en TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (actualizado_en, tramite_id)
) PARTITION BY RANGE (actualizado_en);

CREATE TABLE tramites.vista_tramite_p2024
    PARTITION OF tramites.vista_tramite_particionada
    FOR VALUES FROM ('2024-01-01 00:00:00+00') TO ('2025-01-01 00:00:00+00');

CREATE TABLE tramites.vista_tramite_p2025
    PARTITION OF tramites.vista_tramite_particionada
    FOR VALUES FROM ('2025-01-01 00:00:00+00') TO ('2026-01-01 00:00:00+00');

CREATE TABLE tramites.vista_tramite_p2026
    PARTITION OF tramites.vista_tramite_particionada
    FOR VALUES FROM ('2026-01-01 00:00:00+00') TO ('2027-01-01 00:00:00+00');

-- Verificar Partition Pruning: solo escanea la partición 2026 (`vista_tramite_p2026`)
EXPLAIN
SELECT tramite_id, codigo, estado
FROM tramites.vista_tramite_particionada
WHERE actualizado_en >= '2026-01-01 00:00:00+00'
  AND actualizado_en <  '2026-06-01 00:00:00+00';

-- ------------------------------------------------------------------------------
-- PASO 5: DROP INDEX para dejar el ejercicio repetible en clase
-- ------------------------------------------------------------------------------
DROP INDEX IF EXISTS tramites.idx_vista_tramite_dni;
DROP INDEX IF EXISTS tramites.idx_vista_tramite_keyset;
DROP TABLE IF EXISTS tramites.vista_tramite_particionada CASCADE;

