-- ==============================================================================
-- V1__esquema_citas.sql — Esquema para el microservicio de Citas (M1)
-- Tablas: oficina_consular, cupo, cita
-- Secuencia: cita_codigo_seq
-- ==============================================================================

CREATE SCHEMA IF NOT EXISTS citas;

CREATE SEQUENCE IF NOT EXISTS citas.cita_codigo_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS citas.oficina_consular (
    id VARCHAR(5) PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    pais CHAR(2) NOT NULL,
    ciudad VARCHAR(60) NOT NULL,
    zona_horaria VARCHAR(40) NOT NULL
);

CREATE TABLE IF NOT EXISTS citas.cupo (
    id UUID PRIMARY KEY,
    oficina_id VARCHAR(5) NOT NULL REFERENCES citas.oficina_consular(id),
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    capacidad INT NOT NULL CHECK (capacidad > 0),
    ocupados INT NOT NULL DEFAULT 0 CHECK (ocupados >= 0 AND ocupados <= capacidad),
    version INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_cupo_oficina_fecha_hora UNIQUE (oficina_id, fecha, hora)
);

-- Índice para acelerar la consulta de disponibilidad diaria por oficina consular
CREATE INDEX IF NOT EXISTS idx_cupo_oficina_fecha ON citas.cupo(oficina_id, fecha);

CREATE TABLE IF NOT EXISTS citas.cita (
    id UUID PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    dni CHAR(8) NOT NULL,
    nombres VARCHAR(120) NOT NULL,
    email VARCHAR(120) NOT NULL,
    oficina_id VARCHAR(5) NOT NULL REFERENCES citas.oficina_consular(id),
    cupo_id UUID NOT NULL REFERENCES citas.cupo(id),
    tipo_tramite VARCHAR(20) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    notificacion VARCHAR(20) NOT NULL,
    creada_en TIMESTAMPTZ NOT NULL
);

-- Índice para consulta de citas de un ciudadano por DNI
CREATE INDEX IF NOT EXISTS idx_cita_dni ON citas.cita(dni);

-- Índice compuesto para validar citas duplicadas el mismo día en la misma oficina
CREATE INDEX IF NOT EXISTS idx_cita_dni_oficina_estado ON citas.cita(dni, oficina_id, estado);

