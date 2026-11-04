---
description: Reglas de Docker, Docker Compose y compatibilidad con el equipo del participante (Windows + Docker Desktop)
activation: glob
glob: "**/Dockerfile,**/docker-compose*.yml,**/*.sh,**/*.ps1,**/.env*"
---

# Docker y entorno del participante

## Entorno objetivo

- Windows 10/11 64 bits, Docker Desktop con WSL 2 (o Rancher Desktop / Podman Desktop con compose compatible), 8 GB de RAM como mínimo.
- El participante **no** tiene Maven ni Node instalados. Sí tiene Git, JDK 21 y Spring Tools for Eclipse.
- Debe funcionar igual en macOS/Linux, pero Windows es el caso principal.

## Dockerfiles Java (multi-etapa)

```dockerfile
# Etapa 1: compilación (no requiere Maven en el equipo)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
COPY <servicio>/pom.xml <servicio>/
RUN mvn -q -pl <servicio> -am dependency:go-offline
COPY <servicio>/src <servicio>/src
RUN mvn -q -pl <servicio> -am package -DskipTests

# Etapa 2: ejecución
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /src/<servicio>/target/*.jar app.jar
USER app
EXPOSE <puerto>
HEALTHCHECK --interval=10s --timeout=3s --start-period=40s --retries=5 \
  CMD wget -qO- http://localhost:<puerto>/actuator/health | grep -q UP || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
```

- El contexto de build es la carpeta del módulo (`m1-estilos`, `m2-integracion`, …) para que funcione el POM agregador.
- Usar caché de Maven con `--mount=type=cache,target=/root/.m2` (BuildKit está activo por defecto en Docker Desktop).
- Límite de memoria por servicio Java en compose: `mem_limit: 512m` (gateway y BFF: `384m`).

## Docker Compose

- Nombre de proyecto fijo con `name:` (`m1`, `m2`, `m3`, `m4`) para que los nombres de contenedor y red sean predecibles en las guías.
- `container_name` explícito y legible (`m1-monolito`, `m2-tramites`, …) porque las guías y la clase usan comandos como `docker stop m1-postgres`.
- Redes nombradas: `frontal` (frontend, gateway, puntos de entrada) y `datos` (bases de datos, broker, caché). Los servicios que acceden a datos se conectan a ambas. Así funciona la simulación de partición con `docker network disconnect m1_datos m1-monolito`.
- `depends_on` con `condition: service_healthy`. Health checks en PostgreSQL (`pg_isready`), RabbitMQ (`rabbitmq-diagnostics ping`), Redis (`redis-cli ping`).
- `restart: unless-stopped` en servicios de aplicación (demostración de disponibilidad 1.1).
- Volúmenes nombrados para datos; `docker compose down -v` reinicia todo.
- PostgreSQL se inicializa con `initdb/01-esquemas.sql` (crea esquemas y usuarios por servicio).
- Variables en `.env` versionado con valores de laboratorio (no hay secretos reales). Incluir `.env.example` idéntico con comentarios.
- Perfiles de compose cuando el módulo tiene variantes u opcionales (`monolito`, `microservicios`, `serverless`, `clean`, `carga`, `sidecar`, `calidad`). Los servicios de infraestructura comunes (postgres, mailpit, frontend) no llevan perfil o llevan todos los perfiles necesarios para que siempre se levanten.
- Fijar versiones de imágenes de terceros por etiqueta mayor/menor (no `latest`): `postgres:16-alpine`, `rabbitmq:4-management-alpine` (o la mayor estable vigente), `redis:7-alpine`, `axllent/mailpit:<versión>`, `wiremock/wiremock:3.x`, `grafana/otel-lgtm:<versión>`, `structurizr/lite:<versión>`, `grafana/k6:<versión>`, `envoyproxy/envoy:<versión estable>`, `nginx:1.27-alpine` o vigente.

## Scripts de apoyo

- Cada módulo incluye `scripts/` con equivalentes `.ps1` (PowerShell, principal) y `.sh`: `levantar`, `detener`, `reiniciar-datos`, `simular-particion`, `restaurar-red`.
- Los scripts imprimen en español qué hacen y qué debe observar el participante.

## Fin de línea y codificación

- `.gitattributes` con `* text=auto eol=lf`, `*.ps1 text eol=crlf`, `*.sh text eol=lf`. Crítico: scripts `.sh` con CRLF fallan dentro de contenedores Linux.
- Archivos en UTF-8 sin BOM.

## Recursos

- Presupuesto total por módulo en ejecución: ≤ 6 GB de RAM (M4 es el más pesado). Medir con `docker stats` en la verificación y registrar el valor en el README del módulo.
- Tiempo de primer `docker compose up --build` documentado (aprox.) en el README del módulo.
