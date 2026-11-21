# Guía del Docente para Publicación y Gestión del Repositorio en GitHub

Este documento detalla el procedimiento para publicar el repositorio `mrree-arquitectura-software` en GitHub, configurar las reglas de protección de ramas, administrar la publicación progresiva de las ramas de solución (`solucion/a1` a `solucion/a4`) tras el cierre de cada entrega y enviar las instrucciones iniciales a los participantes.

---

## 1. Creación del Repositorio Remoto y Primera Publicación

### 1.1 Crear el repositorio en GitHub (Interfaz Web o GitHub CLI `gh`)

Cree un repositorio **vacío** (sin `README.md`, sin `.gitignore` y sin licencia inicial, ya que todo está versionado localmente) bajo la organización o cuenta del curso:

```powershell
# Opción A: Usando GitHub CLI (gh)
gh repo create <organizacion-o-docente>/mrree-arquitectura-software --public --description "Plataforma Digital de Servicios Consulares y de Legalización — Curso Arquitectura de Software y Diseño de Soluciones (UTEC Posgrado – MRREE)"
```

### 1.2 Configurar el remoto `origin` y publicar `main`, `demo/strangler` y etiquetas `s1`…`s6`

> [!IMPORTANT]
> **Antes del inicio de las clases**, publique únicamente la rama `main`, la rama de demostración `demo/strangler` y las etiquetas de sesión `s1`…`s6`. **Retenga** las ramas `solucion/a1` a `solucion/a4` en su máquina local para publicarlas una por una después de la fecha límite de entrega de cada asignación (ver Sección 3).

```powershell
# 1. Configurar el remoto origin (si aún no está registrado)
git remote add origin https://github.com/<organizacion-o-docente>/mrree-arquitectura-software.git
# (O si ya existe: git remote set-url origin https://github.com/<organizacion-o-docente>/mrree-arquitectura-software.git)

# 2. Publicar la rama principal (main) y la rama de demostración de la Sesión 6 (demo/strangler)
git push -u origin main
git push -u origin demo/strangler

# 3. Publicar todas las etiquetas de punto de partida de sesión (s1 a s6)
git push origin --tags
```

*(Si desea publicar todas las ramas, incluidas las soluciones, de una sola vez en un repositorio privado de respaldo del docente, puede usar `git push origin --all` y `git push origin --tags`).*

---

## 2. Reglas de Protección de Ramas (`main` y `solucion/*`)

Para evitar modificaciones accidentales o *force-push* sobre la rama base del curso y las soluciones de referencia:

### 2.1 Desde la Interfaz Web de GitHub
1. Ingrese a **Settings > Branches > Add branch ruleset** (o **Add branch protection rule**).
2. **Regla para `main`:**
   - *Branch name pattern:* `main`
   - Activar **Require a pull request before merging**.
   - Activar **Require status checks to pass before merging** y seleccionar los jobs del workflow `.github/workflows/ci.yml` (`build-m1`, `build-m2`, `build-m3`, `build-m4`, `build-frontend`).
   - Activar **Do not allow bypassing the above settings** y bloquear *force pushes* y eliminación de la rama.
3. **Regla para `solucion/*` y `demo/*`:**
   - *Branch name pattern:* `solucion/*` (y `demo/*`)
   - Bloquear *force pushes* y eliminación de ramas para que las soluciones publicadas permanezcan inmutables.

### 2.2 Automatización con GitHub CLI (`gh api`)

```powershell
# Proteger la rama main exigiendo que el workflow CI pase en verde
gh api --method PUT "repos/<organizacion-o-docente>/mrree-arquitectura-software/branches/main/protection" `
  -H "Accept: application/vnd.github+json" `
  -F "required_Linear_history=true" `
  -F "allow_force_pushes=false" `
  -F "allow_deletions=false" `
  -F "enforce_admins=false" `
  -f "required_pull_request_reviews=null" `
  -f "restrictions=null"
```

---

## 3. Calendario de Publicación de las Ramas de Solución (`solucion/a1` … `solucion/a4`)

Cada asignación se libera al cierre de su sesión correspondiente y se entrega antes del inicio de la siguiente sesión. Publique cada rama de solución **únicamente después de cerrada la ventana de entrega**:

| Asignación | Sesión en que se deja | Momento de publicación de la solución | Comando exacto para publicar la solución |
| --- | --- | --- | --- |
| **Asignación A1** (`m1-estilos`) | Al finalizar **Sesión 2** | Al iniciar la **Sesión 3** (tras cierre de entrega A1) | `git push -u origin solucion/a1` |
| **Asignación A2** (`m2-integracion`) | Al finalizar **Sesión 3** | Al iniciar la **Sesión 4** (tras cierre de entrega A2) | `git push -u origin solucion/a2` |
| **Asignación A3** (`m3-datos`) | Al finalizar **Sesión 4** | Al iniciar la **Sesión 5** (tras cierre de entrega A3) | `git push -u origin solucion/a3` |
| **Asignación A4** (`m3-datos` + `docs/c4`) | Al finalizar **Sesión 5** | Al iniciar la **Sesión 6** (tras cierre de entrega A4) | `git push -u origin solucion/a4` |

Una vez publicada una rama de solución, los participantes pueden inspeccionar el `diff` exacto en GitHub mediante la URL:
`https://github.com/<organizacion-o-docente>/mrree-arquitectura-software/compare/main...solucion/a1`

---

## 4. Plantilla de Mensaje de Bienvenida a los Participantes

Copie y adapte el siguiente mensaje para enviarlo por correo o publicarlo en el anuncio inicial del aula virtual **48 horas antes de la Sesión 1**:

```markdown
Asunto: [Arquitectura de Software – MRREE / UTEC] Preparación del entorno y repositorio oficial del curso

Estimados participantes:

Bienvenidos al curso **Arquitectura de Software y Diseño de Soluciones**. A lo largo de nuestras 6 sesiones trabajaremos sobre una plataforma de referencia completa (**Plataforma Digital de Servicios Consulares y de Legalización**), construida con Java 21, Spring Boot 3.4, Angular 19, PostgreSQL, RabbitMQ, Redis, OpenTelemetry, Grafana y Structurizr C4.

El repositorio oficial del curso ya se encuentra disponible en:
🔗 https://github.com/<organizacion-o-docente>/mrree-arquitectura-software

### Pasos previos a la Sesión 1 (Tiempo estimado: 30–40 minutos)

Para aprovechar al máximo los laboratorios prácticos desde el primer día, les solicitamos completar las guías paso a paso ubicadas en la carpeta `docs/instalacion/` del repositorio:

1. **Git y cuenta en GitHub (`docs/instalacion/01-git-y-github.md`):**
   Configurar `git config --global core.autocrlf false` (indispensable en Windows).
2. **Docker Desktop con WSL 2 (`docs/instalacion/02-docker-desktop.md`):**
   O su alternativa libre Rancher Desktop (con motor `dockerd/moby`). No necesitan instalar Maven, Node.js, PostgreSQL ni RabbitMQ localmente.
3. **JDK 21 LTS (Eclipse Temurin) y Spring Tools 4 for Eclipse (`docs/instalacion/03-jdk-y-eclipse.md`).**
4. **Crear su Fork personal, clonar y levantar el Módulo 1 (`docs/instalacion/04-fork-clonar-ejecutar.md`):**
   - Realicen un **Fork** del repositorio hacia su cuenta de GitHub.
   - Clonen su fork y ejecuten:
     ```powershell
     cd m1-estilos
     .\scripts\levantar.ps1 -Perfil monolito
     ```
   - Verifiquen en su navegador que carguen correctamente:
     - Portal Ciudadano: http://localhost:4200
     - Swagger UI: http://localhost:8081/swagger-ui.html
     - Bandeja de correos (Mailpit): http://localhost:8025
   - Al terminar la prueba, detengan y limpien los contenedores con:
     ```powershell
     .\scripts\limpiar.ps1 -Forzar
     ```

Si presentan cualquier duda o inconveniente durante la instalación, consulten la guía `docs/instalacion/solucion-de-problemas.md` o escríbanos en el foro técnico del aula virtual adjuntando la salida del comando `docker compose ps`.

¡Nos vemos en la Sesión 1!

Atentamente,
Equipo Docente – Arquitectura de Software y Diseño de Soluciones (UTEC Posgrado)
```
