-- ==============================================================================
-- V1__esquema_notificaciones.sql — Esquema para el microservicio de Notificaciones
-- Tabla: aviso
-- ==============================================================================

CREATE SCHEMA IF NOT EXISTS notificaciones;

CREATE TABLE IF NOT EXISTS notificaciones.aviso (
    id UUID PRIMARY KEY,
    destinatario VARCHAR(120) NOT NULL,
    asunto VARCHAR(160) NOT NULL,
    cuerpo TEXT NOT NULL,
    canal VARCHAR(10) NOT NULL,
    estado VARCHAR(10) NOT NULL,
    creado_en TIMESTAMPTZ NOT NULL,
    referencia VARCHAR(40)
);

-- Índice para búsqueda rápida de avisos por destinatario
CREATE INDEX IF NOT EXISTS idx_aviso_destinatario ON notificaciones.aviso(destinatario);

-- Índice para consulta de avisos asociados al código de referencia (ej. CIT-2026-000001)
CREATE INDEX IF NOT EXISTS idx_aviso_referencia ON notificaciones.aviso(referencia);

