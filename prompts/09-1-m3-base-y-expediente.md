# Prompt 09-1 — M3: base de `m3-datos` y expediente documental

> Rama `main`. Referencias: contrato (secciones 2, 3, 6, 6.3), reglas 01–05. Antes de crear o modificar archivos, genera un Implementation Plan y espera mi aprobación. No modifiques `m1-estilos` ni `m2-integracion`.

## Objetivo didáctico

Dejar `m3-datos` autosuficiente y listo para el laboratorio de persistencia de la sesión 4 (contenido 3.1): un esquema y un usuario por servicio, migraciones Flyway visibles en Eclipse y un modelo documental (JSONB) dentro de PostgreSQL.

## Alcance

1. **Copia autosuficiente.** `m3-datos/` copia los servicios de `m2-integracion` de `main` (citas, tramites, identidad, pide-simulador, notificaciones, pagos, bff-web, gateway, envoy opcional) y el frontend en modo m3. **No** referencia carpetas de M2. **Excluye** el andamiaje de A2 (endpoint de observación, cola `notificaciones.tramite.observado` y su prueba): no forma parte de M3.
2. **Compose** `m3-datos/docker-compose.yml`: `name: m3` en la primera línea; contenedores `m3-<servicio>` (por ejemplo `m3-postgres`, `m3-rabbitmq`, `m3-tramites`, `m3-pagos`, `m3-gateway`, `m3-frontend`); redes con nombre fijo `m3_frontal` y `m3_datos`; volumen `m3_postgres_data`; Redis 7 (`m3-redis`, 6379) incluido desde ya; frontend con `MODULO=m3`.
3. **Esquemas y usuarios** en `m3-datos/postgres/initdb/01-esquemas.sql`: un esquema por servicio (`citas`, `tramites`, `pagos`, `emision`, `notificaciones`, `identidad`) y un usuario `<esquema>_app` con permisos **solo** sobre su esquema. Comentarios en el SQL que expliquen el patrón "base de datos por servicio".
4. **Migraciones Flyway** de cada servicio en `src/main/resources/db/migration/<esquema>/`, numeradas desde `V1__`.
5. **Expediente documental** en tramites-service:
   - Tabla `tramites.expediente(tramite_id uuid pk, metadatos jsonb not null)` con índice GIN (`idx_expediente_metadatos`) y comentario que explique el enfoque documental.
   - Claves de `metadatos` según el tipo: `paisDestino` (apostilla y registro consular), `folios` (legalización), `idioma` (todos).
   - Al registrar un trámite se guardan sus metadatos variables.
   - `GET /api/tramites/buscar?pais=ES` consulta con el operador `@>` sobre JSONB.
6. **Scripts** en `m3-datos/scripts/` (`.ps1` y `.sh`), con los mismos requisitos de robustez del prompt 07: verificación de `docker info`, corte si `docker compose` falla, espera de `healthy` con `{{if .State.Health}}…{{else}}{{.State.Status}}{{end}}` y tiempo máximo, aviso si los puertos 8080, 8081 o 4200 están ocupados por M1 o M2:
   - `levantar`, `detener`, `limpiar` (con `-Forzar` / `--forzar`).
   - `psql`: abre `psql` dentro de `m3-postgres` como administrador sobre la base `consular`.

## Pruebas

- Integración (Testcontainers) de `GET /api/tramites/buscar?pais=ES`.
- Prueba que verifica que el usuario `pagos_app` no puede leer `tramites.expediente`.

## Criterios de aceptación

- `cd m3-datos ; .\scripts\levantar.ps1` deja todos los contenedores `m3-*` en `healthy`; el flujo de M2 sigue funcionando a través del gateway.
- `.\scripts\psql.ps1` → `\dn` muestra un esquema por servicio; `\d tramites.expediente` muestra el índice GIN.
- La consulta documental de la presentación funciona tal cual:
  `SELECT v.codigo, e.metadatos->>'idioma' AS idioma FROM tramites.expediente e JOIN tramites.vista_tramite v USING (tramite_id) WHERE e.metadatos @> '{"paisDestino":"ES"}';`
  (si `vista_tramite` aún no existe, se crea en 09-2; en este paso probar con `tramites.tramite` y dejar la consulta final documentada en el README).
- `mvn -f m3-datos verify` pasa; `git status` sin cambios en `m1-estilos` ni `m2-integracion`.

## Commit

`feat(m3): base de m3-datos, esquemas por servicio y expediente JSONB`
