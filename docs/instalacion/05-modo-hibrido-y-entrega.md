# Paso 5: Modo de Ejecución Híbrido (Eclipse + Docker) y Flujo de Entrega de Asignaciones

Cuando desarrolle las asignaciones prácticas (**A1** a **A4**) o el **Proyecto Final**, no necesita reconstruir la imagen Docker en cada cambio de código. El **modo híbrido** le permite mantener la infraestructura y los demás servicios en Docker mientras ejecuta y depura (**Debug**) el servicio que está modificando directamente desde **Spring Tools for Eclipse** usando el perfil de Spring Boot **`local`**.

---

## 1. Cómo Ejecutar un Servicio en Modo Híbrido (Docker + Perfil `local` en Eclipse)

Todos los servicios Java del repositorio incluyen un archivo `src/main/resources/application-local.yml` preconfigurado para conectarse a `localhost` en los puertos publicados por Docker (`5432` para PostgreSQL, `5672` para RabbitMQ, `6379` para Redis y `1025` para Mailpit).

### Ejemplo con `m1-estilos/monolito-modular` (Asignación A1)

1. **Levante el módulo completo en Docker** (o solo la infraestructura):
   ```powershell
   cd m1-estilos
   .\scripts\levantar.ps1 -Perfil monolito
   ```
2. **Detenga únicamente el contenedor del servicio Java** que desea ejecutar desde Eclipse (`monolito`), dejando `m1-postgres`, `m1-mailpit` y `m1-frontend` encendidos:
   ```powershell
   docker compose stop monolito
   ```
3. **En Spring Tools for Eclipse:**
   - Localice la clase principal `MonolitoApplication.java` dentro de `m1-estilos/monolito-modular`.
   - Haga clic derecho sobre ella y seleccione **Run As > Run Configurations...** (o **Debug As > Debug Configurations...**).
   - Cree una configuración dentro de **Spring Boot App** (o **Java Application**).
   - En el campo **Profile** (o en la pestaña *Arguments > VM arguments* agregando `-Dspring.profiles.active=local`), escriba:
     ```text
     local
     ```
   - Haga clic en **Apply** y luego en **Run** (o **Debug**).

![captura: Configuración de ejecución en Spring Tools for Eclipse con el perfil activo local](img/05-eclipse-run-profile-local.png)

4. El servicio arrancará en pocos segundos en el mismo puerto (`8081` en M1, `8084` para `notificaciones-service` en A2, `8082` para `tramites-service` en A3/A4) y el frontend en `http://localhost:4200` o el API Gateway en `http://localhost:8080` se comunicarán con él o podrá invocarlo directamente en su puerto local.

### Tabla de Servicios y Puertos por Asignación en Modo Híbrido

| Asignación | Módulo | Contenedor a detener (`docker compose stop ...`) | Proyecto en Eclipse | Perfil Spring | Puerto local |
| --- | --- | --- | --- | --- | --- |
| **A1** | `m1-estilos` | `docker compose stop monolito` | `monolito-modular` (`MonolitoApplication`) | `local` | `8081` |
| **A2** | `m2-integracion` | `docker compose stop notificaciones-service` | `notificaciones-service` (`NotificacionesApplication`) | `local` | `8084` |
| **A3** | `m3-datos` | `docker compose stop tramites-service` | `tramites-service` (`TramitesApplication`) | `local` | `8082` |
| **A4** | `m3-datos` | `docker compose stop tramites-service` | `tramites-service` (`TramitesApplication`) | `local` | `8082` |
| **Proyecto Final** | `m4-observabilidad` | `docker compose stop tramites-service` | `tramites-service` (`TramitesApplication`) | `local` | `8082` |

---

## 2. Ejecutar las Pruebas Automatizadas (En Contenedor o desde Eclipse)

Antes de dar por terminada una asignación, recuerde habilitar la prueba marcada con `@Disabled("Asignación AN")` y verificar que toda la suite de pruebas (incluyendo `ReglasDeArquitecturaTest` con ArchUnit) pase en verde:

### Opción A: Con Maven en Contenedor (sin instalar Maven local)

```powershell
# Desde la raíz del repositorio (ejemplo para A1 en m1-estilos):
docker run --rm -v "${PWD}/m1-estilos:/w" -v "mre-m2:/root/.m2" -w /w maven:3.9.9-eclipse-temurin-21 mvn -pl monolito-modular test
```

### Opción B: Desde Spring Tools for Eclipse

- Haga clic derecho sobre la clase de prueba (por ejemplo, `ReprogramarCitaServiceTest.java`) > **Run As > JUnit Test**.

---

## 3. Flujo Completo de Entrega de una Asignación (`aN-<usuario>`)

Para cada asignación (**A1**, **A2**, **A3**, **A4**) y para el **Proyecto Final** (`pf-<usuario>`), siga este procedimiento estandarizado sobre **su fork**:

### Paso 3.1: Ubicarse en `main` actualizado y crear la rama de la asignación

```powershell
# 1. Asegurarse de estar en la rama main limpia
git checkout main

# 2. Crear la rama de la asignación con su usuario de GitHub (ejemplo: a1-jperez)
git checkout -b a1-jperez
```

### Paso 3.2: Desarrollar la Tarea Conceptual y la Tarea Práctica

Realice únicamente los cambios solicitados en la guía correspondiente de [`docs/asignaciones/`](../asignaciones/). Verifique con `git status` y `git diff --stat` qué archivos ha modificado:

```powershell
git status -s
git diff --stat
```

### Paso 3.3: Hacer Commit y Subir (`push`) la Rama a su Fork (`origin`)

```powershell
# 1. Agregar al área de preparación los archivos de la asignación
git add docs/adr/ADR-001-estilo-y-consistencia.md
git add m1-estilos/monolito-modular/src/main/java/pe/utec/consular/monolito/citas/domain/ReglaDeReprogramacion.java
git add m1-estilos/monolito-modular/src/test/java/pe/utec/consular/monolito/citas/ReprogramarCitaServiceTest.java

# 2. Registrar el commit con un mensaje descriptivo en español
git commit -m "feat(a1): completar ADR-001 y regla de reprogramación de citas"

# 3. Publicar la rama en su fork personal (origin)
git push -u origin a1-jperez
```

### Paso 3.4: Obtener el Enlace de Entrega

1. Abra su *fork* en GitHub (`https://github.com/<su-usuario>/mrree-arquitectura-software`).
2. Seleccione su rama `a1-<su-usuario>` en el selector de ramas o abra la vista de comparación:
   `https://github.com/<su-usuario>/mrree-arquitectura-software/tree/a1-<su-usuario>`
3. Verifique en la pestaña **Actions** de su *fork* que el flujo de Integración Continua (`CI`) haya finalizado en verde (**✓**).
4. Envíe el enlace de su rama en el aula virtual según las indicaciones del docente.

![captura: Rama a1-usuario publicada en el fork de GitHub con check de CI en verde](img/05-github-rama-entrega.png)

---

## 4. Cómo Sincronizar su Fork con el Repositorio Oficial (`upstream`)

Si el docente publica actualizaciones o etiquetas de sesión (`s1` … `s6`) en el repositorio oficial (`upstream`), sincronice la rama `main` de su copia local y de su *fork* ejecutando:

```powershell
# 1. Volver a la rama main local
git checkout main

# 2. Descargar los nuevos commits y etiquetas desde el repositorio oficial (upstream)
git fetch upstream --tags

# 3. Actualizar su rama main local con fast-forward
git merge --ff-only upstream/main

# 4. Actualizar su fork en GitHub (origin)
git push origin main --tags
```

---

**Siguiente paso:** Si encuentra algún inconveniente técnico durante la instalación o ejecución, consulte [Solución de Problemas Frecuentes (`solucion-de-problemas.md`)](solucion-de-problemas.md).

