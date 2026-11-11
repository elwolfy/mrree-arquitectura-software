-- ==============================================================================
-- V1__esquema_tramites.sql — Esquema y tarifas base para el módulo de Tarifas
-- Tabla: tarifa_base
-- ==============================================================================

CREATE SCHEMA IF NOT EXISTS tramites;

CREATE TABLE IF NOT EXISTS tramites.tarifa_base (
    tipo_tramite VARCHAR(20) PRIMARY KEY,
    monto NUMERIC(10,2) NOT NULL,
    moneda CHAR(3) NOT NULL
);

-- Semilla de tarifas base según regla 7 del contrato
INSERT INTO tramites.tarifa_base (tipo_tramite, monto, moneda) VALUES
('LEGALIZACION', 20.00, 'PEN'),
('APOSTILLA', 30.00, 'PEN')
ON CONFLICT (tipo_tramite) DO NOTHING;

