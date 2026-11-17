-- ==============================================================================
-- V1__esquema_tramites.sql — Esquema de base de datos para tramites-service (M2)
-- ==============================================================================

CREATE SCHEMA IF NOT EXISTS tramites;

CREATE SEQUENCE IF NOT EXISTS tramites.tramite_codigo_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS tramites.tramite (
    id           UUID PRIMARY KEY,
    codigo       VARCHAR(20) NOT NULL UNIQUE,
    dni          CHAR(8) NOT NULL,
    nombres      VARCHAR(120) NOT NULL,
    email        VARCHAR(120) NOT NULL,
    tipo_tramite VARCHAR(25) NOT NULL,
    subtipo      VARCHAR(30) NOT NULL,
    pais         CHAR(2) NOT NULL,
    folios       INT NOT NULL CHECK (folios > 0),
    idioma       VARCHAR(10) NOT NULL,
    estado       VARCHAR(20) NOT NULL,
    monto        NUMERIC(10, 2) NOT NULL,
    creado_en    TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_tramite_dni ON tramites.tramite(dni);

