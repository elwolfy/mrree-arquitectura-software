# Plataforma Digital de Servicios Consulares y de Legalización

Repositorio didáctico oficial del curso **Arquitectura de Software y Diseño de Soluciones** (UTEC Posgrado – MRREE). Contiene una aplicación de referencia completa y evolutiva (**Java 21 LTS / Spring Boot 3.4.x** en el backend y **Angular 19 / PrimeNG 19** en el frontend) que demuestra en código ejecutable los patrones, atributos de calidad, mecanismos de integración, datos distribuidos, observabilidad y gobernanza técnica a lo largo de las 6 sesiones del programa.

> [!NOTE]
> **Aviso de datos de laboratorio:**  
> Todos los datos (oficinas consulares, ciudadanos, DNI, trámites, tarifas y constancias) son **estrictamente ficticios y con fines didácticos**. No corresponden ni representan información real del Ministerio de Relaciones Exteriores del Perú (MRREE) ni de persona alguna.

---

## 1. Módulos del Curso

Cada módulo es **autocontenido**, se ubica en su propia carpeta, posee su propio `pom.xml` y `docker-compose.yml`, y se opera mediante scripts de ciclo de vida uniformes (`levantar`, `detener`, `limpiar`).

| Carpeta | Sesiones | Conceptos Arquitectónicos Demostrados | Cómo Levantar (PowerShell / Bash) |
| :--- | :--- | :--- | :--- |
| [`m1-estilos/`](m1-estilos/README.md) | **S1 – S2** | Atributos de calidad, Monolito Modular Evolutivo (Spring Modulith), Arquitectura Hexagonal vs. Clean Architecture (ArchUnit), Microservicios, Serverless (FaaS), Teorema CAP/PACELC (`CP` vs. `AP`), SOLID y patrones GoF (*Factory*, *Strategy*, *Adapter*) | `cd m1-estilos; .\scripts\levantar.ps1 -Perfil monolito`<br>*(perfiles: `monolito`, `clean`, `microservicios`, `serverless`)* |
| [`m2-integracion/`](m2-integracion/README.md) | **S3** | API Gateway (`:8080`), BFF Web (`:8085`), gRPC vs. REST (`:9082`), Resiliencia (Circuit Breaker, Retry, Timeout), EDA con RabbitMQ y DLQ (`consular.eventos`), Patrón Sidecar (Envoy), DDD Estratégico y ACL (`RENIEC/PIDE`) | `cd m2-integracion; .\scripts\levantar.ps1`<br>*(perfil opcional: `-Perfil sidecar`)* |
| [`m3-datos/`](m3-datos/README.md) | **S4 – S5** | *Database-per-Service* (esquemas y roles aislados), JSONB + índice GIN (`tramites.expediente`), Event Sourcing (`eventos_tramite`), CQRS (`vista_tramite`), Transactional Outbox (`RelayOutbox`), Saga Orquestada con compensación, Idempotencia (`Idempotency-Key` y consumidor), Optimización SQL (1M filas, *Keyset*) y Caché Redis | `cd m3-datos; .\scripts\levantar.ps1` |
| [`m4-observabilidad/`](m4-observabilidad/README.md) | **S5 – S6** | OpenTelemetry Java Agent (trazas distribuidas W3C `traceparent` en HTTP, gRPC, RabbitMQ y Outbox), Grafana LGTM (`:3000` — Loki, Grafana, Tempo, Prometheus), Métricas Micrometer y SLOs, Modelo C4 en Structurizr Lite (`:8090`), Gobernanza OpenAPI (Spectral), Deuda Técnica (SonarQube `:9000`) y *Strangler Fig* | `cd m4-observabilidad; .\scripts\levantar.ps1`<br>*(C4 ligero: `.\scripts\levantar-c4.ps1`; calidad: `.\scripts\analizar-calidad.ps1`)* |
| [`frontend/`](frontend/) | **S1 – S6** | Portal Ciudadano Angular Standalone + PrimeNG (`:4200`) con selector de módulo activo, reserva y reprogramación de citas, registro y seguimiento de trámites con sondeo asíncrono, simulador de doble clic idempotente y panel de correlación de trazas | Incluido automáticamente en el `docker-compose.yml` de cada módulo (`http://localhost:4200`) |

---

## 2. Mapa Completo de Documentación (`docs/`)

### 2.1 Guías de Instalación y Configuración del Entorno (`docs/instalacion/`)
- [**Índice y Requisitos de Hardware/Software**](docs/instalacion/README.md) (incluye tabla de tiempos de arranque y memoria RAM por módulo)
- [**Paso 1 — Git y Cuenta de GitHub**](docs/instalacion/01-git-y-github.md) (`core.autocrlf false`)
- [**Paso 2 — Docker Desktop y WSL 2**](docs/instalacion/02-docker-desktop.md) (alternativas Rancher / Podman Desktop)
- [**Paso 3 — JDK 21 (Eclipse Temurin) y Spring Tools for Eclipse**](docs/instalacion/03-jdk-y-eclipse.md)
- [**Paso 4 — Fork, Clonar y Ejecutar el Módulo 1**](docs/instalacion/04-fork-clonar-ejecutar.md)
- [**Paso 5 — Modo Híbrido (Eclipse + Docker) y Flujo de Entrega**](docs/instalacion/05-modo-hibrido-y-entrega.md)
- [**Solución de Problemas Frecuentes (*Troubleshooting*)**](docs/instalacion/solucion-de-problemas.md)

### 2.2 Especificación, Glosario, Trazabilidad, DDD y Contratos
- [**Contrato Global de la Aplicación (Fuente de Verdad)**](docs/especificacion/contrato-global.md)
- [**Glosario Técnico del Curso (Secciones 1.1 a 4.4)**](docs/glosario.md)
- [**Matriz de Trazabilidad Plan del Curso ↔ Código Fuente (`1.1` – `4.4`)**](docs/trazabilidad-plan-codigo.md)
- [**Guía del Docente para Publicación en GitHub**](docs/publicacion.md)
- [**DDD Estratégico — Mapa de Contextos (*Context Map*)**](docs/ddd/context-map.md)
- [**DDD Estratégico — Lenguaje Ubicuo por Contexto Delimitado**](docs/ddd/lenguaje-ubicuo.md)
- [**Contrato de Eventos Asíncronos (AsyncAPI 2.6)**](docs/contratos/asyncapi.yaml)

### 2.3 Arquitectura como Código (Modelo C4) y Decisiones Arquitectónicas (ADR)
- [**Modelo C4 en Structurizr DSL (`workspace.dsl`)**](docs/c4/workspace.dsl) y [**Guía de Uso C4 ↔ ArchiMate**](docs/c4/README.md)
- [**Índice y Ciclo de Vida de los ADR**](docs/adr/README.md) y [**Plantilla MADR**](docs/adr/template.md)
  - [ADR-000: Selección de versión de plataforma (Java 21 y Spring Boot 3.4.x)](docs/adr/ADR-000-version-de-plataforma.md)
  - [ADR-001: Estilo arquitectónico inicial y consistencia de citas (Esqueleto A1)](docs/adr/ADR-001-estilo-y-consistencia.md)
  - [ADR-002: Coordinación Solicitud → Pago → Emisión mediante Saga Orquestada](docs/adr/ADR-002-saga-orquestada.md)
  - [ADR-003: Publicación confiable de eventos con Transactional Outbox](docs/adr/ADR-003-outbox-transaccional.md)
  - [ADR-004: Persistencia del agregado Trámite con Event Sourcing y CQRS](docs/adr/ADR-004-event-sourcing-tramite.md)
  - [ADR-005: Observabilidad unificada con OpenTelemetry Java Agent y Grafana LGTM](docs/adr/ADR-005-opentelemetry.md)
  - [ADR-006: Estrategia de caché de lectura para consulta de trámites (Esqueleto A4)](docs/adr/ADR-006-cache-consulta-tramite.md)

### 2.4 Matrices Comparativas, Observabilidad y Estándares de Gobernanza
- **Matrices experimentales (`docs/matrices/`):**
  - [Escenarios de Atributos de Calidad (M1)](docs/matrices/m1-escenarios-calidad.md)
  - [Comparativa Experimental de Estilos: Monolito vs. Microservicios vs. Serverless (M1)](docs/matrices/m1-estilos.md)
  - [Decisiones CAP y PACELC por Operación (M1)](docs/matrices/m1-cap-por-operacion.md)
  - [Arquitectura Hexagonal vs. Clean Architecture (M1)](docs/matrices/m1-hexagonal-vs-clean.md)
  - [Matriz de Optimización SQL sobre 1 000 000 de Filas (M3)](docs/matrices/m3-optimizacion.md)
- **Observabilidad y Estándares (`docs/observabilidad/` y `docs/estandares/`):**
  - [SLIs, SLOs y Presupuesto de Error](docs/observabilidad/slo.md)
  - [Lineamientos de Diseño de APIs REST v0.1](docs/estandares/lineamientos-api.md) y [Reglas Spectral (`.spectral.yaml`)](docs/estandares/.spectral.yaml)
  - [Estándar de Observabilidad (Logs, Métricas y Trazas)](docs/estandares/estandar-observabilidad.md)
  - [Reglas de Arquitectura Automatizadas (ArchUnit y Spring Modulith)](docs/estandares/reglas-arquitectura.md)
  - [Checklist de Revisión de Arquitectura](docs/estandares/checklist-revision-arquitectura.md)

### 2.5 Guías de Asignaciones y Proyecto Final (`docs/asignaciones/`)
- [**Asignación A1 (S2 → S3):** ADR-001 (CAP/PACELC) y Regla de Reprogramación de Citas en `m1-estilos`](docs/asignaciones/A1.md)
- [**Asignación A2 (S3 → S4):** Context Map (`tramite.observado`) y Consumidor RabbitMQ en `m2-integracion`](docs/asignaciones/A2.md)
- [**Asignación A3 (S4 → S5):** Diagrama de Compensación y Paso Compensatorio de la Saga en `m3-datos`](docs/asignaciones/A3.md)
- [**Asignación A4 (S5 → S6):** Redis en C4 + ADR-006 y Adaptador `Cache-Aside` con Redis en `m3-datos`](docs/asignaciones/A4.md)
- [**Proyecto Final Integrador (S5 → S6):** Expiración de Trámites sin Pago en `m4-observabilidad`](docs/asignaciones/proyecto-final.md)

---

## 3. Modelo de Ramas, Etiquetas y Flujo de Trabajo

| Rama / Etiqueta | Propósito | Responsable |
| :--- | :--- | :--- |
| `main` | Aplicación base de referencia completa con el andamiaje de las asignaciones (las pruebas de asignación llevan `@Disabled` y la lógica central contiene `TODO`). | Docente |
| `solucion/a1` … `solucion/a4` | Ramas con la solución de referencia oficial de cada asignación práctica (**A1** a **A4**), con pruebas habilitadas en verde y guía `AN-solucion.md`. | Docente |
| `demo/strangler` | Demostración práctica en vivo del patrón **Strangler Fig** en el API Gateway de `m4-observabilidad` (Sesión 6). | Docente |
| Etiquetas `s1` … `s6` | Puntos de partida verificados para cada una de las 6 sesiones sincrónicas del curso. | Docente |
| `aN-<usuario>` / `pf-<usuario>` | Ramas de trabajo y entrega creadas por cada participante dentro de su propio **fork**. | Participante |
