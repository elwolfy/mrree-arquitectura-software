-- ==============================================================================
-- 01-esquemas.sql — Inicialización de esquemas y usuarios para M1 (Estilos)
-- Base de datos: consular
-- Esquemas: citas, tramites, notificaciones
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
END
$do$;

GRANT ALL PRIVILEGES ON DATABASE consular TO citas_app, tramites_app, notificaciones_app;

-- ------------------------------------------------------------------------------
-- Esquema: citas
-- ------------------------------------------------------------------------------
CREATE SCHEMA IF NOT EXISTS citas AUTHORIZATION citas_app;
REVOKE ALL ON SCHEMA citas FROM PUBLIC;
GRANT ALL PRIVILEGES ON SCHEMA citas TO citas_app;
ALTER DEFAULT PRIVILEGES FOR ROLE citas_app IN SCHEMA citas GRANT ALL ON TABLES TO citas_app;
ALTER DEFAULT PRIVILEGES FOR ROLE citas_app IN SCHEMA citas GRANT ALL ON SEQUENCES TO citas_app;
ALTER DEFAULT PRIVILEGES FOR ROLE citas_app IN SCHEMA citas GRANT ALL ON FUNCTIONS TO citas_app;

-- ------------------------------------------------------------------------------
-- Esquema: tramites
-- ------------------------------------------------------------------------------
CREATE SCHEMA IF NOT EXISTS tramites AUTHORIZATION tramites_app;
REVOKE ALL ON SCHEMA tramites FROM PUBLIC;
GRANT ALL PRIVILEGES ON SCHEMA tramites TO tramites_app;
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
ALTER DEFAULT PRIVILEGES FOR ROLE notificaciones_app IN SCHEMA notificaciones GRANT ALL ON TABLES TO notificaciones_app;
ALTER DEFAULT PRIVILEGES FOR ROLE notificaciones_app IN SCHEMA notificaciones GRANT ALL ON SEQUENCES TO notificaciones_app;
ALTER DEFAULT PRIVILEGES FOR ROLE notificaciones_app IN SCHEMA notificaciones GRANT ALL ON FUNCTIONS TO notificaciones_app;

GRANT ALL PRIVILEGES ON SCHEMA citas TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA tramites TO postgres;
