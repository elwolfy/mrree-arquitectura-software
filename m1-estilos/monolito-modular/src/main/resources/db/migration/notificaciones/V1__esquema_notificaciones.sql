-- ==============================================================================
-- V1__esquema_notificaciones.sql — Esquema para el módulo de Notificaciones
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

CREATE INDEX IF NOT EXISTS idx_aviso_destinatario ON notificaciones.aviso(destinatario);
CREATE INDEX IF NOT EXISTS idx_aviso_referencia ON notificaciones.aviso(referencia);
