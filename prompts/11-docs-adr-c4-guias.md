# Prompt 11 — Documentación: ADR, modelo C4, guías de instalación y asignaciones

> Rama `main`. Referencias: todo el contrato, regla 06, plan del curso (secciones 5, 7, 8 y 10).

## Tareas

### 1. ADR (`docs/adr/`, formato MADR)

- `template.md` (plantilla MADR en español: contexto, opciones consideradas, decisión, consecuencias, estado).
- `ADR-000-version-de-plataforma.md` (Java 21, versión de Spring Boot elegida y por qué).
- `ADR-001-estilo-y-consistencia.md`: **solo esqueleto** con contexto del caso y secciones vacías; el participante lo completa en A1. (La versión completa va en `solucion/a1`.)
- `ADR-002-saga-orquestada.md`, `ADR-003-outbox-transaccional.md`, `ADR-004-event-sourcing-tramite.md`, `ADR-005-opentelemetry.md`: completos, como ejemplos.
- `README.md` con el índice y el ciclo de vida de un ADR.

### 2. Modelo C4 (`docs/c4/workspace.dsl`)

- Structurizr DSL del sistema de M4: personas (Ciudadano, Funcionario consular), sistema "Plataforma consular", sistemas externos (PIDE/RENIEC, Pasarela de pagos — simulada, Servicio de correo).
- Vistas: `SystemContext`, `Container` (frontend, gateway, bff, servicios, PostgreSQL por esquema como un solo contenedor con nota, RabbitMQ, Redis **no incluido** — se agrega en A4), `Component` de tramites-service (controladores, saga, proyector, relay outbox, adaptadores), y una vista dinámica del flujo solicitud → pago → emisión.
- Estilos sobrios; descripciones en español; tecnología en cada contenedor.
- `docs/c4/README.md`: cómo abrir en Structurizr Lite (M4) y cómo exportar diagramas; relación C4 ↔ ArchiMate (contenedor ≈ Application Component; persona ≈ Business Actor; sistema externo ≈ Application Component externo/Interface).

### 3. Guías de instalación (`docs/instalacion/`)

Archivos en español, paso a paso, con capturas marcadas como `![captura: …](img/…)` (marcadores que el docente reemplaza):

- `README.md` (índice y requisitos de hardware).
- `01-git-y-github.md` (Git para Windows, cuenta GitHub, configuración `user.name/email`, `core.autocrlf false`).
- `02-docker-desktop.md` (WSL 2, virtualización, Docker Desktop, verificación `docker run hello-world`; alternativa Rancher/Podman Desktop; nota de licenciamiento).
- `03-jdk-y-eclipse.md` (Temurin 21, Spring Tools for Eclipse, configuración del JRE, import de proyectos Maven).
- `04-fork-clonar-ejecutar.md` (fork, clonar el fork, `upstream`, levantar M1, verificar URLs).
- `05-modo-hibrido-y-entrega.md` (detener un contenedor, ejecutar desde Eclipse con perfil `local`, crear rama `aN-<usuario>`, commit, push, enlace de entrega, sincronizar el fork con `upstream`).
- `solucion-de-problemas.md`.

### 4. Guías de asignación (`docs/asignaciones/`)

`A1.md` … `A4.md` y `proyecto-final.md`, con la estructura de la regla 06, alineadas con el plan (sección 8) y el contrato (sección 8). Incluir el tiempo estimado (A1 60 min, A2 30, A3 30, A4 60, proyecto final 30 + semana entre S5 y S6) y la rúbrica del plan (sección 10).

### 5. Otros

- `docs/ddd/` (si no existe desde el prompt 08).
- `docs/glosario.md` (términos técnicos del curso con enlace a la sección del plan).
- Actualizar el `README.md` raíz con enlaces a todo lo anterior.

## Criterios de aceptación

- `docker run --rm -v "$PWD/docs/c4":/usr/local/structurizr structurizr/cli validate -workspace workspace.dsl` sin errores.
- Con M4 levantado, `http://localhost:8090` muestra las 4 vistas.
- Todos los enlaces relativos de `docs/` resuelven (usar un verificador de enlaces Markdown en contenedor, p. ej. `lycheeverse/lychee --offline`).
- ADR-001 es solo un esqueleto en `main`.

## Commit

`docs: ADR, C4 y guías`
