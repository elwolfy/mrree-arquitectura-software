# Guía de Instalación y Preparación del Entorno de Laboratorio

Esta guía detalla paso a paso cómo preparar su estación de trabajo para ejecutar, explorar y desarrollar los laboratorios y asignaciones del curso **Arquitectura de Software y Diseño de Soluciones** sobre la **Plataforma Digital de Servicios Consulares y de Legalización** (MRREE – UTEC Posgrado).

> [!IMPORTANT]
> **Compilación autocontenida en Docker:** No es necesario instalar Apache Maven, Node.js, npm, PostgreSQL, RabbitMQ, Redis ni Grafana directamente en su sistema operativo host. Todos los módulos (`m1-estilos`, `m2-integracion`, `m3-datos` y `m4-observabilidad`) se compilan y ejecutan mediante imágenes oficiales multi-etapa (`eclipse-temurin:21`, `maven:3.9.9-eclipse-temurin-21`, `node:22-alpine` y `nginx:1.27-alpine`).

---

## 1. Índice de Guías Paso a Paso

Siga estas guías en orden secuencial antes de la primera sesión práctica:

| Paso | Guía | Contenido principal |
| --- | --- | --- |
| **1** | [01 — Git y Cuenta de GitHub](01-git-y-github.md) | Instalación de Git para Windows, creación/verificación de cuenta en GitHub, configuración de `user.name`, `user.email` y `core.autocrlf false`. |
| **2** | [02 — Docker Desktop y WSL 2](02-docker-desktop.md) | Activación de WSL 2 y virtualización en BIOS/UEFI, instalación de Docker Desktop, verificación con `docker run hello-world`, alternativas libres (Rancher Desktop / Podman Desktop) y nota sobre licenciamiento. |
| **3** | [03 — JDK 21 y Spring Tools for Eclipse](03-jdk-y-eclipse.md) | Instalación de Eclipse Temurin JDK 21, Spring Tools 4 for Eclipse (STS), configuración del JRE 21 por defecto e importación de proyectos Maven del repositorio. |
| **4** | [04 — Fork, Clonar y Ejecutar el Primer Módulo](04-fork-clonar-ejecutar.md) | Creación del *fork* personal en GitHub, clonación local, configuración del remoto `upstream`, arranque de `m1-estilos` y verificación de URLs en el navegador. |
| **5** | [05 — Modo Híbrido (Eclipse + Docker) y Flujo de Entrega](05-modo-hibrido-y-entrega.md) | Cómo detener un único contenedor y ejecutar ese microservicio en modo depuración desde Eclipse con el perfil `local`, creación de la rama `aN-<usuario>`, *commit*, *push*, entrega y sincronización del *fork* con `upstream`. |
| **6** | [Solución de Problemas Frecuentes (*Troubleshooting*)](solucion-de-problemas.md) | Diagnóstico y solución de errores habituales: puertos ocupados, finales de línea `CRLF` en scripts `.sh`, memoria en WSL 2 (`vmmem`), Flyway y limpieza de volúmenes. |

---

## 2. Requisitos de Hardware y Software

| Componente | Mínimo Requerido | Recomendado | Notas |
| --- | --- | --- | --- |
| **Procesador (CPU)** | 4 núcleos lógicos (x86_64 / ARM64) con virtualización habilitada | 8 núcleos lógicos o superior | Requiere Intel VT-x o AMD-V activo en BIOS/UEFI. |
| **Memoria RAM del equipo** | **8 GB RAM** (con límite de $\le 6\text{ GB}$ asignado a WSL 2 / Docker) | **16 GB RAM** | Ningún módulo del curso supera los **6 GB de RAM** de consumo total en contenedores (contrato global, sección 1). |
| **Espacio libre en disco** | **15 GB** libres | **25 GB** libres (SSD NVMe) | Incluye imágenes base de Docker, caché de dependencias Maven (`.m2`) y contenedores de observabilidad. |
| **Sistema Operativo** | Windows 10 (22H2+) o Windows 11 de 64 bits | Windows 11 64 bits / macOS 14+ / Ubuntu 22.04+ | Las guías incluyen comandos equivalentes en **PowerShell** (`.ps1`) y **Bash** (`.sh`). |
| **Motor de Contenedores** | Docker Desktop 4.30+ (motor WSL 2) | Docker Desktop / Rancher Desktop (`dockerd` / `moby`) | Debe disponer del comando `docker compose version` (Compose v2). |
| **Java Development Kit** | **JDK 21 LTS** (Eclipse Temurin) | Eclipse Temurin 21.0.x | Requerido para el IDE y para ejecutar pruebas desde la terminal o en modo híbrido. |
| **IDE** | Spring Tools 4 for Eclipse (STS 4.26+) | Spring Tools 4 for Eclipse / IntelliJ IDEA / VS Code | Las guías paso a paso toman **Spring Tools for Eclipse** como referencia oficial del curso. |

---

## 3. Tiempos de Arranque y Consumo de Memoria por Módulo

La siguiente tabla resume las mediciones verificadas de tiempo del primer `docker compose up -d --build` y consumo de memoria RAM en estado estable (`healthy`) para cada módulo y perfil del curso:

| Módulo / Perfil | Comando de arranque (`scripts/`) | Contenedores activos | Tiempo 1.er `up --build` | Tiempo arranque en caliente (con caché) | Memoria RAM total (`docker stats`) | Presupuesto máximo (Contrato Global) |
| --- | --- | --- | --- | --- | --- | --- |
| **`m1-estilos`** (`monolito`) | `.\scripts\levantar.ps1 -Perfil monolito` | 4 (`postgres`, `mailpit`, `monolito`, `frontend`) | ~55 s | ~18 s | **~363 MiB** (~0.36 GB) | $\le 6.0\text{ GB}$ |
| **`m1-estilos`** (`clean`) | `.\scripts\levantar.ps1 -Perfil clean` | 5 (`+ comparativa-clean`) | ~65 s | ~22 s | **~515 MiB** (~0.50 GB) | $\le 6.0\text{ GB}$ |
| **`m1-estilos`** (`microservicios`) | `.\scripts\levantar.ps1 -Perfil microservicios` | 5 (`postgres`, `mailpit`, `citas`, `notificaciones`, `frontend`) | ~70 s | ~24 s | **~639 MiB** (~0.62 GB) | $\le 6.0\text{ GB}$ |
| **`m1-estilos`** (`serverless`) | `.\scripts\levantar.ps1 -Perfil serverless` | 7 (`postgres`, `mailpit`, `router`, `fn-consultas`, `fn-reservar`, `fn-notificar`, `frontend`) | ~85 s | ~28 s | **~897 MiB** (~0.88 GB) | $\le 6.0\text{ GB}$ |
| **`m2-integracion`** (estándar) | `.\scripts\levantar.ps1` | 11 (`postgres`, `rabbitmq`, `mailpit`, `reniec-mock`, 5 microservicios, `bff-web`, `gateway`, `frontend`) | ~125 s | ~38 s | **~1.95 GiB** (~2.09 GB) | $\le 6.0\text{ GB}$ |
| **`m3-datos`** (estándar) | `.\scripts\levantar.ps1` | 12 (`+ redis`, `emision-service`) | ~140 s | ~42 s | **~2.28 GiB** (~2.45 GB) | $\le 6.0\text{ GB}$ |
| **`m4-observabilidad`** (estándar) | `.\scripts\levantar.ps1` | 15 (`+ lgtm` Grafana/Tempo/Loki/Prometheus, `structurizr`) | ~165 s | ~48 s | **~3.17 GiB** (~3.40 GB) | $\le 6.0\text{ GB}$ |
| **`m4-observabilidad`** (`+ calidad`) | `.\scripts\analizar-calidad.ps1` | 16 (`+ sonarqube`) | ~195 s | ~65 s | **~4.35 GiB** (~4.67 GB) | $\le 6.0\text{ GB}$ |

> [!TIP]
> **Regla de oro para cuidar la memoria de su equipo:** Ejecute **un solo módulo a la vez**. Antes de pasar de un módulo a otro (por ejemplo, de `m2-integracion` a `m3-datos`), detenga y limpie siempre el módulo actual ejecutando `.\scripts\limpiar.ps1 -Forzar` (o `./scripts/limpiar.sh --forzar`).
