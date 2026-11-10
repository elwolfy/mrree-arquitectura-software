# Prompt 08-1 — M2: base de `m2-integracion` y `citas-service`

> Rama `main`. Referencias: contrato (secciones 1, 3, 6, 6.1), reglas 01–04. Antes de crear o modificar archivos, genera un Implementation Plan y espera mi aprobación.

## Objetivo didáctico

Preparar la carpeta del Módulo 2 y el primer servicio que publica un evento de integración. En la sesión 3 los participantes verán este evento esperar en la cola cuando el consumidor esté detenido (contenido 2.3).

## Alcance

1. **Estructura** `m2-integracion/` con proyecto Maven multimódulo (`pom.xml` padre, Java 21, misma versión de Spring Boot que M1 o la fijada por ADR-000). Módulos que se crearán en los pasos 08-1 a 08-6: `citas-service`, `tramites-service`, `pagos-service`, `notificaciones-service`, `identidad-service`, `bff-web`, `gateway`. En este paso solo se crea `citas-service`; los demás quedan comentados en el `pom.xml` padre.
2. **Compose** `m2-integracion/docker-compose.yml` (`name: m2`), contenedores con prefijo `m2-` (`m2-postgres`, `m2-rabbitmq`, `m2-mailpit`, `m2-citas`, …):
   - `postgres` 16 con un esquema por bounded context (`citas`, `tramites`, `pagos`, `notificaciones`, `identidad`), creado por script de inicio. **Prohibido compartir tablas entre servicios.**
   - `rabbitmq:3-management` con usuario y contraseña `consular`/`consular`, consola en `15672`, y `rabbitmq/definitions.json` que declara el exchange `consular.eventos` (tipo `topic`), las colas `<servicio>.<evento>` del contrato 6.1 que existen en `main` y sus colas `<cola>.dlq` con *dead-letter exchange* configurado.
   - `mailpit` (1025/8025).
   - *Healthchecks* en todos los contenedores.
3. **`citas-service` (8081)** — hexagonal, paquete `pe.utec.consular.citas`, basado en el módulo de citas del monolito de M1 (se duplica a propósito: sin librerías de dominio compartidas, ver relación "kernel compartido" evitada).
   - Endpoints de citas del contrato.
   - Puerto de salida `application/port/out/PublicadorDeEventosPort` y adaptador `adapter/out/messaging/RabbitPublicadorDeEventos`, que publica `cita.reservada` con las cabeceras del contrato (`messageId`, `tipoEvento`, `ocurridoEn`).
   - Flyway en el esquema `citas`, datos semilla del contrato.
4. **Scripts** `m2-integracion/scripts/levantar.ps1` y `levantar.sh` (construyen, levantan y esperan los *healthchecks*; imprimen las URL de la tabla del contrato) y `detener.(ps1|sh)`.

## Pruebas

- ArchUnit de capas hexagonales en `citas-service`.
- Integración con Testcontainers (PostgreSQL + RabbitMQ): reservar una cita publica exactamente un mensaje `cita.reservada` con `messageId`.

## Criterios de aceptación

- `cd m2-integracion ; .\scripts\levantar.ps1` deja `m2-postgres`, `m2-rabbitmq`, `m2-mailpit` y `m2-citas` en estado *healthy*.
- Reservar una cita por Swagger (`localhost:8081/swagger-ui.html`) deja un mensaje visible en la cola de la consola RabbitMQ (`localhost:15672`, `consular`/`consular`) mientras no exista consumidor.
- `mvn -f m2-integracion verify` pasa.

## Commit

`feat(m2): base de integración y citas-service con eventos`
