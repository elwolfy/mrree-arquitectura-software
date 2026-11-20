# Paso 4: Crear su Fork, Clonar el Repositorio y Ejecutar el Módulo 1

En el curso cada participante trabaja sobre su propio **Fork** en GitHub y mantiene una referencia al repositorio oficial del curso (`upstream`) para recibir actualizaciones si fuera necesario.

---

## 1. Realizar el Fork del Repositorio Oficial en GitHub

1. Ingrese con su cuenta de GitHub a la página del repositorio oficial del curso:
   `https://github.com/<organizacion-o-docente>/mrree-arquitectura-software`
2. En la esquina superior derecha, haga clic en el botón **Fork** > **Create a new fork**.
3. En el formulario de creación del *fork*:
   - Mantenga el nombre `mrree-arquitectura-software`.
   - **Desmarque** la casilla *Copy the `main` branch only* si desea disponer también de las etiquetas y ramas de demostración en su copia.
   - Haga clic en **Create fork**.

![captura: Creación del Fork personal en la interfaz web de GitHub](img/04-github-crear-fork.png)

Una vez finalizado, la URL de su *fork* personal será:
`https://github.com/<su-usuario>/mrree-arquitectura-software.git`

---

## 2. Clonar su Fork Localmente y Configurar `upstream`

1. Abra **PowerShell**, ubíquese en una carpeta corta de su disco (por ejemplo, `C:\repos`) y clone **su fork**:

```powershell
mkdir C:\repos -ErrorAction SilentlyContinue
cd C:\repos

# Reemplace <su-usuario> por su usuario de GitHub
git clone https://github.com/<su-usuario>/mrree-arquitectura-software.git
cd mrree-arquitectura-software
```

2. Registre el repositorio oficial del docente como remoto `upstream` para poder sincronizar cambios:

```powershell
# Reemplace <organizacion-o-docente> por la cuenta oficial indicada por el docente
git remote add upstream https://github.com/<organizacion-o-docente>/mrree-arquitectura-software.git

# Verifique que aparezcan origin (su fork) y upstream (el repositorio oficial)
git remote -v
```

Salida esperada:

```text
origin    https://github.com/<su-usuario>/mrree-arquitectura-software.git (fetch)
origin    https://github.com/<su-usuario>/mrree-arquitectura-software.git (push)
upstream  https://github.com/<organizacion-o-docente>/mrree-arquitectura-software.git (fetch)
upstream  https://github.com/<organizacion-o-docente>/mrree-arquitectura-software.git (push)
```

![captura: Salida de git remote -v mostrando origin y upstream en PowerShell](img/04-git-remote-v.png)

---

## 3. Levantar el Módulo 1 (`m1-estilos`) por Primera Vez

Asegúrese de que **Docker Desktop** esté en ejecución y ejecute el script de arranque del **Módulo 1** con el perfil `monolito`:

### En Windows (PowerShell)

```powershell
cd m1-estilos
.\scripts\levantar.ps1 -Perfil monolito
```

### En Linux / macOS (Bash)

```bash
cd m1-estilos
./scripts/levantar.sh monolito
```

El script compilará las imágenes multi-etapa y esperará automáticamente hasta que todos los contenedores reporten estado **`healthy`**:

```text
[OK] m1-postgres -> healthy
[OK] m1-mailpit -> healthy
[OK] m1-monolito -> healthy
[OK] m1-frontend -> healthy
```

![captura: Salida del script levantar.ps1 -Perfil monolito con los 4 contenedores healthy](img/04-m1-levantar-healthy.png)

---

## 4. Verificar las URLs de la Plataforma en su Navegador

Con el módulo levantado, abra las siguientes direcciones para confirmar que todo su entorno funciona de extremo a extremo:

| Componente | URL | Qué debe observar |
| --- | --- | --- |
| **Frontend Angular (Ciudadano)** | http://localhost:4200 | Portal de Reserva de Citas Consulares con las 6 oficinas consulares (`MAD`, `BUE`, `SCL`, `MIA`, `MIL`, `TYO`). |
| **Swagger UI (OpenAPI 3)** | http://localhost:8081/swagger-ui.html | Documentación interactiva de los endpoints `/api/oficinas`, `/api/citas` y `/api/sistema/info`. |
| **Información del Estilo Activo** | http://localhost:8081/api/sistema/info | JSON `{"estilo":"MONOLITO_MODULAR","consistencia":"CP","instancia":"monolito-1"}`. |
| **Bandeja de Correos (Mailpit)** | http://localhost:8025 | Interfaz web para inspeccionar los correos SMTP enviados al reservar una cita. |
| **Actuator Health** | http://localhost:8081/actuator/health | JSON `{"status":"UP"}`. |

![captura: Portal Ciudadano en http://localhost:4200 mostrando el catálogo de oficinas consulares](img/04-frontend-4200.png)

---

## 5. Detener o Limpiar el Módulo al Terminar

- Para **detener** los contenedores conservando los datos en PostgreSQL:
  ```powershell
  .\scripts\detener.ps1
  ```
- Para **detener y eliminar contenedores y volúmenes** (recomendado antes de cambiar de perfil o de módulo):
  ```powershell
  .\scripts\limpiar.ps1 -Forzar
  ```

---

**Siguiente paso:** Continúe con [05 — Modo Híbrido (Eclipse + Docker) y Flujo de Entrega](05-modo-hibrido-y-entrega.md).

