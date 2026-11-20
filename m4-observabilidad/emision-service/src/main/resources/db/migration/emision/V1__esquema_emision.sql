-- ==============================================================================
-- V1__esquema_emision.sql — Esquema de base de datos para emision-service (M3)
-- ==============================================================================

CREATE SCHEMA IF NOT EXISTS emision;

CREATE SEQUENCE IF NOT EXISTS emision.documento_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS emision.documento_emitido (
    id               UUID PRIMARY KEY,
    tramite_id       UUID NOT NULL UNIQUE,
    numero_documento VARCHAR(30) NOT NULL UNIQUE,
    emitido_en       TIMESTAMPTZ NOT NULL
);

-- Tabla de idempotencia del consumidor de emision.comandos
CREATE TABLE IF NOT EXISTS emision.mensaje_procesado (
    message_id   VARCHAR(100) PRIMARY KEY,
    procesado_en TIMESTAMPTZ NOT NULL
);

