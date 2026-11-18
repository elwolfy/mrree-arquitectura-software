-- ==============================================================================
-- V1__esquema_notificaciones.sql — Esquema para notificaciones-service (M2)
-- Incluye tabla de avisos y tabla de idempotencia mensaje_procesado
-- ==============================================================================

CREATE SCHEMA IF NOT EXISTS notificaciones;

CREATE TABLE IF NOT EXISTS notificaciones.aviso (
    id           UUID PRIMARY KEY,
    dni          CHAR(8) NOT NULL,
    destinatario VARCHAR(120) NOT NULL,
    asunto       VARCHAR(160) NOT NULL,
    cuerpo       TEXT NOT NULL,
    canal        VARCHAR(10) NOT NULL,
    estado       VARCHAR(10) NOT NULL,
    creado_en    TIMESTAMPTZ NOT NULL,
    referencia   VARCHAR(40) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_aviso_dni ON notificaciones.aviso(dni);

CREATE TABLE IF NOT EXISTS notificaciones.mensaje_procesado (
    message_id   UUID PRIMARY KEY,
    tipo_evento  VARCHAR(60) NOT NULL,
    procesado_en TIMESTAMPTZ NOT NULL
);

