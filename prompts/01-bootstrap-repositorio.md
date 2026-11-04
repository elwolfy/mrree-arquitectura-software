# Prompt 01 — Estructura base del repositorio

> Pegar en el panel Agent de Antigravity, en modo Planning. Rama: `main`.

## Contexto

Vas a crear la estructura base del repositorio `mrree-arquitectura-software`, material didáctico del curso "Arquitectura de Software y Diseño de Soluciones". Lee primero `@AGENTS.md`, todas las reglas de `.agents/rules/` y `@docs/especificacion/contrato-global.md`. No generes todavía código de negocio.

## Tareas

1. **Árbol de carpetas** exactamente como en `AGENTS.md`, más:
   - `docs/{instalacion,asignaciones,adr,c4,matrices,pruebas/k6,pruebas/sql,pruebas/colecciones,estandares,especificacion}`
   - `.github/workflows/`
2. **Archivos raíz:**
   - `README.md` del repositorio en español: propósito, tabla de módulos (carpeta, sesiones, conceptos, cómo levantar), requisitos del equipo (enlazar a `docs/instalacion/`), modelo de ramas (main protegida, trabajo en fork), aviso de datos ficticios.
   - `.gitignore` (Java/Maven, Eclipse: `.metadata/`, `.settings/`, `.classpath`, `.project` solo de raíz no; Node/Angular, IDE, `.env.local`).
   - `.gitattributes` según la regla 03 (fin de línea).
   - `LICENSE` MIT con titular "Curso Arquitectura de Software – UTEC Posgrado (material didáctico)".
   - `.editorconfig` (UTF-8, LF, indentación 4 en Java, 2 en TS/JSON/YAML).
3. **POM agregador por módulo** (`m1-estilos/pom.xml`, `m2-integracion/pom.xml`, `m3-datos/pom.xml`, `m4-observabilidad/pom.xml`): `packaging pom`, padre `spring-boot-starter-parent` (versión del contrato), `<modules>` vacío por ahora, propiedades de versiones (Java 21, Spring Cloud, springdoc, ArchUnit, Testcontainers, Spring Modulith, Resilience4j), `dependencyManagement` con los BOM correspondientes, plugin JaCoCo en perfil `cobertura`. Configurar `maven-surefire-plugin` para excluir `@Tag("integracion")` y `maven-failsafe-plugin` para incluirlas.
4. **`initdb/01-esquemas.sql`** dentro de cada módulo: crea esquemas y usuarios del contrato (`citas`, `tramites`, `notificaciones` en m1; en m2+ además `pagos`, `identidad`, `emision`), con permisos solo sobre su esquema.
5. **CI**: `.github/workflows/ci.yml` según la regla 04 (matriz por carpeta de módulo; job de frontend que solo corre si existe `frontend/package.json`).
6. **Archivos de colaboración**: `CONTRIBUTING.md` (para docentes: flujo de PR hacia `main`), `.github/pull_request_template.md`.
7. `docs/especificacion/` ya existe (no lo modifiques).

## Criterios de aceptación

- `tree -L 2` coincide con la estructura de `AGENTS.md`.
- `docker run --rm -v "$PWD/m1-estilos":/w -w /w maven:3.9-eclipse-temurin-21 mvn -q validate` termina sin errores en las cuatro carpetas.
- `.gitattributes` presente y correcto.
- No hay código de negocio.

## Commit

`chore: estructura base del repositorio`
