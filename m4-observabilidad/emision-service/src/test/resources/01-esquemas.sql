DO
$do$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'emision_app') THEN
      CREATE USER emision_app WITH PASSWORD 'emision_pwd';
   END IF;
END
$do$;

GRANT ALL PRIVILEGES ON DATABASE consular TO emision_app;
CREATE SCHEMA IF NOT EXISTS emision AUTHORIZATION emision_app;
REVOKE ALL ON SCHEMA emision FROM PUBLIC;
GRANT ALL PRIVILEGES ON SCHEMA emision TO emision_app;
GRANT ALL PRIVILEGES ON SCHEMA emision TO postgres;
ALTER ROLE emision_app SET search_path TO emision;
ALTER DEFAULT PRIVILEGES FOR ROLE emision_app IN SCHEMA emision GRANT ALL ON TABLES TO emision_app;
ALTER DEFAULT PRIVILEGES FOR ROLE emision_app IN SCHEMA emision GRANT ALL ON SEQUENCES TO emision_app;
ALTER DEFAULT PRIVILEGES FOR ROLE emision_app IN SCHEMA emision GRANT ALL ON FUNCTIONS TO emision_app;

