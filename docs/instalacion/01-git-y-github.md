# Paso 1: Instalación de Git y Configuración de Cuenta en GitHub

En este paso instalará **Git** en su equipo y configurará su identidad y el manejo de saltos de línea (`LF`) para evitar errores al construir contenedores Linux desde Windows.

---

## 1. Crear o Verificar su Cuenta en GitHub

1. Ingrese a https://github.com y cree una cuenta gratuita (o inicie sesión con su cuenta existente).
2. Anote su nombre de usuario de GitHub (`<usuario>`) y el correo electrónico asociado, ya que los utilizará para crear su *fork* y nombrar sus ramas de asignación (`a1-<usuario>`, `a2-<usuario>`, `a3-<usuario>`, `a4-<usuario>`).

![captura: Página de inicio de sesión y perfil de usuario en GitHub](img/01-github-cuenta.png)

---

## 2. Instalar Git para Windows

1. Descargue el instalador oficial de **Git for Windows** (64-bit) desde https://git-scm.com/download/win.
2. Ejecute el instalador manteniendo las opciones recomendadas por defecto, prestando especial atención a:
   - **Adjusting your PATH environment:** seleccione *Git from the command line and also from 3rd-party software*.
   - **Configuring the line ending conversions:** seleccione **Checkout as-is, commit Unix-style line endings** (`core.autocrlf = input` o `false`).

![captura: Selección de conversión de saltos de línea en el instalador de Git para Windows](img/01-git-instalador-autocrlf.png)

3. Abra una nueva ventana de **PowerShell** y verifique la versión instalada:

```powershell
git --version
```

Deberá observar una salida similar a `git version 2.45.0.windows.1` (versión 2.40 o superior).

---

## 3. Configurar Identidad y Finales de Línea (`core.autocrlf false`)

Antes de clonar el repositorio, ejecute los siguientes comandos en **PowerShell** (reemplazando su nombre y correo real):

```powershell
# 1. Configurar su nombre y correo electrónico para los commits
git config --global user.name "Juan Pérez"
git config --global user.email "jperez@ejemplo.gob.pe"

# 2. OBLIGATORIO en Windows: desactivar la conversión automática a CRLF
# Esto garantiza que los scripts .sh y archivos de configuración funcionen dentro de contenedores Linux
git config --global core.autocrlf false
git config --global core.eol lf
```

> [!WARNING]
> **¿Por qué es indispensable `core.autocrlf false`?**
> Si Git convierte los saltos de línea de `LF` a `CRLF` al clonar en Windows, los scripts o archivos montados dentro de contenedores Linux fallarán con el error `/usr/bin/env: 'bash\r': No such file or directory`. El repositorio incluye además un archivo `.gitattributes` en la raíz que refuerza `eol=lf`, pero configurar `core.autocrlf false` evita cualquier alteración local.

---

## 4. Verificar la Configuración

Compruebe que los valores hayan quedado registrados ejecutando:

```powershell
git config --global --list
```

Salida esperada:

```text
user.name=Juan Pérez
user.email=jperez@ejemplo.gob.pe
core.autocrlf=false
core.eol=lf
```

![captura: Verificación de git config --global --list en PowerShell](img/01-git-config-list.png)

---

**Siguiente paso:** Continúe con [02 — Docker Desktop y WSL 2](02-docker-desktop.md).

