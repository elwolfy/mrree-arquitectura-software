-- ==============================================================================
-- V1__esquema_pagos.sql — Esquema de base de datos para pagos-service (M3)
-- ==============================================================================

CREATE SCHEMA IF NOT EXISTS pagos;

-- ------------------------------------------------------------------------------
-- 1. PAGOS IDEMPOTENTES — pagos.pago
-- ------------------------------------------------------------------------------
-- La restricción UNIQUE (clave_idempotencia) garantiza a nivel de motor de base
-- de datos que dos peticiones POST /api/pagos con la misma cabecera Idempotency-Key
-- jamás generen un cobro duplicado.
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS pagos.pago (
    id                 UUID PRIMARY KEY,
    tramite_id         UUID NOT NULL,
    monto              NUMERIC(10, 2) NOT NULL CHECK (monto > 0),
    estado             VARCHAR(15) NOT NULL,
    clave_idempotencia VARCHAR(64) NOT NULL UNIQUE,
    creado_en          TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_pago_tramite_id ON pagos.pago(tramite_id);

-- ------------------------------------------------------------------------------
-- 2. TRANSACTIONAL OUTBOX PROPIO DE PAGOS — pagos.outbox
-- ------------------------------------------------------------------------------
-- Evita la escritura doble entre PostgreSQL (esquema pagos) y RabbitMQ al publicar
-- los eventos `pago.confirmado` y `pago.anulado`.
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS pagos.outbox (
    id           UUID PRIMARY KEY,
    agregado_id  UUID NOT NULL,
    tipo         VARCHAR(60) NOT NULL,
    carga        JSONB NOT NULL,
    traceparent  VARCHAR(128) NULL,
    creado_en    TIMESTAMPTZ NOT NULL,
    publicado_en TIMESTAMPTZ NULL
);

CREATE INDEX IF NOT EXISTS idx_pagos_outbox_pendientes
    ON pagos.outbox (creado_en)
    WHERE publicado_en IS NULL;

