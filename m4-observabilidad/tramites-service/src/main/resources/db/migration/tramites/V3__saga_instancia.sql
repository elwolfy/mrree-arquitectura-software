-- ==============================================================================
-- V3__saga_instancia.sql — Tabla de estado del Orquestador de la Saga en tramites-service (M3)
-- ==============================================================================

CREATE TABLE IF NOT EXISTS tramites.saga_instancia (
    tramite_id     UUID PRIMARY KEY,
    paso           VARCHAR(40) NOT NULL,
    estado         VARCHAR(20) NOT NULL,
    actualizado_en TIMESTAMPTZ NOT NULL
);

