-- ==============================================================================
-- V2__event_sourcing_cqrs_outbox.sql — Event Sourcing, CQRS y Outbox en tramites-service (M3)
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 1. EVENT SOURCING — tramites.eventos_tramite (Event Store / Append-Only)
-- ------------------------------------------------------------------------------
-- CONCEPTO ARQUITECTÓNICO:
-- En lugar de sobrescribir el estado actual del trámite en una fila mutable, el Event Store
-- almacena la secuencia inmutable de hechos de dominio (TramiteRegistrado, PagoAsociado,
-- EmisionSolicitada, TramiteEmitido, EmisionConError, TramiteRechazado, PagoAnulacionSolicitada).
-- REGLA DE INMUTABILIDAD: Esta tabla admite SOLO INSERCIONES (append-only); jamás se ejecutan
-- sentencias UPDATE ni DELETE sobre eventos_tramite.
-- CONCURRENCIA OPTIMISTA: La restricción UNIQUE (tramite_id, version) actúa como cerrojo
-- optimista a nivel de flujo de eventos del agregado: si dos hilos intentan persistir
-- simultáneamente la misma versión N para el mismo trámite, PostgreSQL rechaza la segunda
-- inserción evitando pérdida de actualizaciones.
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tramites.eventos_tramite (
    id          BIGSERIAL PRIMARY KEY,
    tramite_id  UUID NOT NULL,
    version     INT NOT NULL CHECK (version > 0),
    tipo        VARCHAR(40) NOT NULL,
    datos       JSONB NOT NULL,
    ocurrido_en TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_eventos_tramite_version UNIQUE (tramite_id, version)
);

-- ------------------------------------------------------------------------------
-- 2. PROYECCIÓN CQRS (Modelo de Lectura) — tramites.vista_tramite
-- ------------------------------------------------------------------------------
-- CONCEPTO ARQUITECTÓNICO:
-- Separa el modelo de escritura basado en eventos (eventos_tramite) del modelo de lectura
-- desnormalizado (vista_tramite) mantenido por ProyectorVistaTramite. Todas las consultas
-- (GET /api/tramites/{id} y GET /api/tramites?dni=) leen exclusivamente de esta vista.
-- La columna motivo_rechazo forma parte del andamiaje de la Asignación A3.
--
-- NOTA DIDÁCTICA (SIN ÍNDICE POR DNI):
-- Deliberadamente NO se crea ningún índice sobre la columna `dni` en esta migración
-- (la tabla solo posee su clave primaria `tramite_id`). El índice `idx_vista_tramite_dni`
-- se crea durante el laboratorio de optimización de consultas de la Sesión 5 para comparar
-- experimentalmente `Seq Scan` vs. `Index Scan` con EXPLAIN ANALYZE sobre 1 000 000 de filas.
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tramites.vista_tramite (
    tramite_id       UUID PRIMARY KEY,
    codigo           VARCHAR(20) NOT NULL,
    dni              CHAR(8) NOT NULL,
    tipo_tramite     VARCHAR(25) NOT NULL,
    estado           VARCHAR(20) NOT NULL,
    monto            NUMERIC(10, 2) NOT NULL,
    motivo_rechazo   VARCHAR(250),
    actualizado_en   TIMESTAMPTZ NOT NULL,
    nombres          VARCHAR(120) NOT NULL DEFAULT '',
    email            VARCHAR(120) NOT NULL DEFAULT '',
    subtipo          VARCHAR(30) NOT NULL DEFAULT '',
    pais             CHAR(2) NOT NULL DEFAULT 'PE',
    folios           INT NOT NULL DEFAULT 1,
    idioma           VARCHAR(10) NOT NULL DEFAULT 'ES',
    pago_id          UUID,
    numero_documento VARCHAR(40),
    creado_en        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ------------------------------------------------------------------------------
-- 3. TRANSACTIONAL OUTBOX — tramites.outbox
-- ------------------------------------------------------------------------------
-- CONCEPTO ARQUITECTÓNICO:
-- Evita el problema de la escritura doble (Dual Write) entre PostgreSQL y RabbitMQ:
-- el evento de integración se inserta en `tramites.outbox` dentro de la misma transacción
-- ACID que persiste los eventos de dominio y actualiza la proyección. Posteriormente,
-- RelayOutbox publica los registros con `publicado_en IS NULL` hacia RabbitMQ y marca
-- la fecha de publicación.
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tramites.outbox (
    id           UUID PRIMARY KEY,
    agregado_id  UUID NOT NULL,
    tipo         VARCHAR(60) NOT NULL,
    carga        JSONB NOT NULL,
    traceparent  VARCHAR(128) NULL,
    creado_en    TIMESTAMPTZ NOT NULL,
    publicado_en TIMESTAMPTZ NULL
);

CREATE INDEX IF NOT EXISTS idx_outbox_pendientes
    ON tramites.outbox (creado_en)
    WHERE publicado_en IS NULL;

