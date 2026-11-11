-- ==============================================================================
-- 01-esquemas.sql — Inicialización de esquemas y usuarios para M2 (Integración)
-- Base de datos: consular
-- Esquemas: citas, tramites, notificaciones, pagos, identidad, emision
-- ==============================================================================

-- Creación idempotente de roles de aplicación
DO
$do$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'citas_app') THEN
      CREATE USER citas_app WITH PASSWORD 'citas_pwd';
   END IF;
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'tramites_app') THEN
      CREATE USER tramites_app WITH PASSWORD 'tramites_pwd';
   END IF;
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'notificaciones_app') THEN
      CREATE USER notificaciones_app WITH PASSWORD 'notificaciones_pwd';
   END IF;
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'pagos_app') THEN
      CREATE USER pagos_app WITH PASSWORD 'pagos_pwd';
   END IF;
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'identidad_app') THEN
      CREATE USER identidad_app WITH PASSWORD 'identidad_pwd';
   END IF;
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'emision_app') THEN
      CREATE USER emision_app WITH PASSWORD 'emision_pwd';
   END IF;
END
$do$;

GRANT ALL PRIVILEGES ON DATABASE consular TO citas_app, tramites_app, notificaciones_app, pagos_app, identidad_app, emision_app;

-- ------------------------------------------------------------------------------
-- Esquema: citas
-- ------------------------------------------------------------------------------
CREATE SCHEMA IF NOT EXISTS citas AUTHORIZATION citas_app;
REVOKE ALL ON SCHEMA citas FROM PUBLIC;
GRANT ALL PRIVILEGES ON SCHEMA citas TO citas_app;
GRANT ALL PRIVILEGES ON SCHEMA citas TO postgres;
ALTER ROLE citas_app SET search_path TO citas;
ALTER DEFAULT PRIVILEGES FOR ROLE citas_app IN SCHEMA citas GRANT ALL ON TABLES TO citas_app;
ALTER DEFAULT PRIVILEGES FOR ROLE citas_app IN SCHEMA citas GRANT ALL ON SEQUENCES TO citas_app;
ALTER DEFAULT PRIVILEGES FOR ROLE citas_app IN SCHEMA citas GRANT ALL ON FUNCTIONS TO citas_app;

-- ------------------------------------------------------------------------------
-- Esquema: tramites
-- ------------------------------------------------------------------------------
CREATE SCHEMA IF NOT EXISTS tramites AUTHORIZATION tramites_app;
REVOKE ALL ON SCHEMA tramites FROM PUBLIC;
GRANT ALL PRIVILEGES ON SCHEMA tramites TO tramites_app;
GRANT ALL PRIVILEGES ON SCHEMA tramites TO postgres;
ALTER ROLE tramites_app SET search_path TO tramites;
ALTER DEFAULT PRIVILEGES FOR ROLE tramites_app IN SCHEMA tramites GRANT ALL ON TABLES TO tramites_app;
ALTER DEFAULT PRIVILEGES FOR ROLE tramites_app IN SCHEMA tramites GRANT ALL ON SEQUENCES TO tramites_app;
ALTER DEFAULT PRIVILEGES FOR ROLE tramites_app IN SCHEMA tramites GRANT ALL ON FUNCTIONS TO tramites_app;

-- ------------------------------------------------------------------------------
-- Esquema: notificaciones
-- ------------------------------------------------------------------------------
CREATE SCHEMA IF NOT EXISTS notificaciones AUTHORIZATION notificaciones_app;
REVOKE ALL ON SCHEMA notificaciones FROM PUBLIC;
GRANT ALL PRIVILEGES ON SCHEMA notificaciones TO notificaciones_app;
GRANT ALL PRIVILEGES ON SCHEMA notificaciones TO postgres;
ALTER ROLE notificaciones_app SET search_path TO notificaciones;
ALTER DEFAULT PRIVILEGES FOR ROLE notificaciones_app IN SCHEMA notificaciones GRANT ALL ON TABLES TO notificaciones_app;
ALTER DEFAULT PRIVILEGES FOR ROLE notificaciones_app IN SCHEMA notificaciones GRANT ALL ON SEQUENCES TO notificaciones_app;
ALTER DEFAULT PRIVILEGES FOR ROLE notificaciones_app IN SCHEMA notificaciones GRANT ALL ON FUNCTIONS TO notificaciones_app;

-- ------------------------------------------------------------------------------
-- Esquema: pagos
-- ------------------------------------------------------------------------------
CREATE SCHEMA IF NOT EXISTS pagos AUTHORIZATION pagos_app;
REVOKE ALL ON SCHEMA pagos FROM PUBLIC;
GRANT ALL PRIVILEGES ON SCHEMA pagos TO pagos_app;
GRANT ALL PRIVILEGES ON SCHEMA pagos TO postgres;
ALTER ROLE pagos_app SET search_path TO pagos;
ALTER DEFAULT PRIVILEGES FOR ROLE pagos_app IN SCHEMA pagos GRANT ALL ON TABLES TO pagos_app;
ALTER DEFAULT PRIVILEGES FOR ROLE pagos_app IN SCHEMA pagos GRANT ALL ON SEQUENCES TO pagos_app;
ALTER DEFAULT PRIVILEGES FOR ROLE pagos_app IN SCHEMA pagos GRANT ALL ON FUNCTIONS TO pagos_app;

-- ------------------------------------------------------------------------------
-- Esquema: identidad
-- ------------------------------------------------------------------------------
CREATE SCHEMA IF NOT EXISTS identidad AUTHORIZATION identidad_app;
REVOKE ALL ON SCHEMA identidad FROM PUBLIC;
GRANT ALL PRIVILEGES ON SCHEMA identidad TO identidad_app;
GRANT ALL PRIVILEGES ON SCHEMA identidad TO postgres;
ALTER ROLE identidad_app SET search_path TO identidad;
ALTER DEFAULT PRIVILEGES FOR ROLE identidad_app IN SCHEMA identidad GRANT ALL ON TABLES TO identidad_app;
ALTER DEFAULT PRIVILEGES FOR ROLE identidad_app IN SCHEMA identidad GRANT ALL ON SEQUENCES TO identidad_app;
ALTER DEFAULT PRIVILEGES FOR ROLE identidad_app IN SCHEMA identidad GRANT ALL ON FUNCTIONS TO identidad_app;

-- ------------------------------------------------------------------------------
-- Esquema: emision
-- ------------------------------------------------------------------------------
CREATE SCHEMA IF NOT EXISTS emision AUTHORIZATION emision_app;
REVOKE ALL ON SCHEMA emision FROM PUBLIC;
GRANT ALL PRIVILEGES ON SCHEMA emision TO emision_app;
GRANT ALL PRIVILEGES ON SCHEMA emision TO postgres;
ALTER ROLE emision_app SET search_path TO emision;
ALTER DEFAULT PRIVILEGES FOR ROLE emision_app IN SCHEMA emision GRANT ALL ON TABLES TO emision_app;
ALTER DEFAULT PRIVILEGES FOR ROLE emision_app IN SCHEMA emision GRANT ALL ON SEQUENCES TO emision_app;
ALTER DEFAULT PRIVILEGES FOR ROLE emision_app IN SCHEMA emision GRANT ALL ON FUNCTIONS TO emision_app;
