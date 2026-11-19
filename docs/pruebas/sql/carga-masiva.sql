-- ==============================================================================
-- carga-masiva.sql — Generación de 1 000 000 de filas en tramites.vista_tramite (M3 - Sesión 5)
-- Tiempo estimado de ejecución en Docker Desktop: ~8 a 18 segundos.
-- ==============================================================================

\timing on

BEGIN;

-- Limpiar datos previos de carga masiva para permitir re-ejecución idempotente
DELETE FROM tramites.vista_tramite WHERE codigo LIKE 'MAS-%';
DELETE FROM tramites.eventos_tramite WHERE datos->>'codigo' LIKE 'MAS-%';

-- 1. Insertar 1 000 000 de filas en tramites.vista_tramite con distribución realista:
--    - Las primeras 40 filas corresponden al ciudadano de prueba DNI '40000001' (>= 30 trámites).
--    - Las restantes 999 960 filas se distribuyen entre ~200 000 ciudadanos distintos.
--    - Estados distribuidos: EMITIDO (60%), PENDIENTE_PAGO (15%), PAGADO (10%), EN_EMISION (10%), RECHAZADO (5%).
--    - Fechas distribuidas a lo largo de los últimos 24 meses (730 días).
INSERT INTO tramites.vista_tramite (
    tramite_id,
    codigo,
    dni,
    tipo_tramite,
    estado,
    monto,
    motivo_rechazo,
    actualizado_en,
    nombres,
    email,
    subtipo,
    pais,
    folios,
    idioma,
    creado_en
)
SELECT
    md5('m3-masivo-' || g::text)::uuid AS tramite_id,
    'MAS-2026-' || lpad(g::text, 7, '0') AS codigo,
    CASE
        WHEN g <= 40 THEN '40000001'
        ELSE lpad((40000002 + (g % 200000))::text, 8, '0')
    END AS dni,
    CASE (g % 3)
        WHEN 0 THEN 'APOSTILLA'
        WHEN 1 THEN 'LEGALIZACION'
        ELSE 'REGISTRO_CONSULAR'
    END AS tipo_tramite,
    CASE
        WHEN (g % 20) < 12 THEN 'EMITIDO'
        WHEN (g % 20) < 15 THEN 'PENDIENTE_PAGO'
        WHEN (g % 20) < 17 THEN 'PAGADO'
        WHEN (g % 20) < 19 THEN 'EN_EMISION'
        ELSE 'RECHAZADO'
    END AS estado,
    CASE (g % 3)
        WHEN 0 THEN 33.00
        WHEN 1 THEN 22.00
        ELSE 27.50
    END AS monto,
    CASE
        WHEN (g % 20) = 19 THEN 'Falla simulada de validación documental'
        ELSE NULL
    END AS motivo_rechazo,
    NOW() - ((g % 730) || ' days')::interval - ((g % 86400) || ' seconds')::interval AS actualizado_en,
    'Ciudadano Laboratorio ' || (g % 200000) AS nombres,
    'ciudadano' || (g % 200000) || '@correo.lab' AS email,
    CASE (g % 3)
        WHEN 0 THEN 'Apostilla'
        WHEN 1 THEN 'Legalizacion'
        ELSE 'RegistroConsular'
    END AS subtipo,
    CASE (g % 5)
        WHEN 0 THEN 'ES'
        WHEN 1 THEN 'AR'
        WHEN 2 THEN 'CL'
        WHEN 3 THEN 'US'
        ELSE 'IT'
    END AS pais,
    1 + (g % 3) AS folios,
    'ES' AS idioma,
    NOW() - ((g % 730) || ' days')::interval - ((g % 86400) || ' seconds')::interval AS creado_en
FROM generate_series(1, 1000000) AS g;

-- 2. Insertar eventos mínimos en tramites.eventos_tramite para los 40 trámites del DNI '40000001'
INSERT INTO tramites.eventos_tramite (tramite_id, version, tipo, datos, ocurrido_en)
SELECT
    v.tramite_id,
    1 AS version,
    'TramiteRegistrado' AS tipo,
    jsonb_build_object(
        'codigo', v.codigo,
        'dni', v.dni,
        'nombres', v.nombres,
        'email', v.email,
        'tipoTramite', v.tipo_tramite,
        'pais', v.pais,
        'folios', v.folios,
        'idioma', v.idioma,
        'monto', v.monto
    ) AS datos,
    v.creado_en
FROM tramites.vista_tramite v
WHERE v.dni = '40000001'
  AND v.codigo LIKE 'MAS-%'
ON CONFLICT (tramite_id, version) DO NOTHING;

COMMIT;

-- Actualizar estadísticas del planificador de PostgreSQL tras la carga masiva
ANALYZE tramites.vista_tramite;
ANALYZE tramites.eventos_tramite;

-- Resumen de verificación
SELECT COUNT(*) AS total_filas_vista_tramite FROM tramites.vista_tramite;
SELECT COUNT(*) AS tramites_dni_40000001 FROM tramites.vista_tramite WHERE dni = '40000001';

