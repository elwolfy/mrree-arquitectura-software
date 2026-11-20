# Paso 2: Instalación de Docker Desktop y Configuración de WSL 2

Todos los módulos del curso (`m1-estilos`, `m2-integracion`, `m3-datos` y `m4-observabilidad`) se levantan mediante **Docker Compose** sobre contenedores Linux. En Windows, esto requiere tener habilitada la virtualización por hardware y el subsistema **WSL 2**.

---

## 1. Verificar la Virtualización por Hardware

1. Abra el **Administrador de tareas** (`Ctrl + Shift + Esc`) y vaya a la pestaña **Rendimiento > CPU**.
2. Verifique que el indicador **Virtualización** muestre **Habilitado** (*Enabled*).
3. Si aparece como *Deshabilitado*, reinicie el equipo, ingrese a la configuración de BIOS/UEFI y active **Intel Virtualization Technology (VT-x)** o **AMD SVM Mode**.

![captura: Administrador de tareas de Windows mostrando Virtualización Habilitada en la pestaña CPU](img/02-administrador-tareas-virtualizacion.png)

---

## 2. Habilitar y Actualizar WSL 2 en Windows

1. Abra **PowerShell como Administrador** y ejecute:

```powershell
wsl --install --no-distribution
wsl --set-default-version 2
wsl --update
```

2. Si el sistema se lo solicita, reinicie Windows para completar la activación de la plataforma de máquina virtual.
3. Tras reiniciar, compruebe el estado de WSL ejecutando:

```powershell
wsl --status
```

Deberá indicar **Versión predeterminada: 2** (*Default Version: 2*).

---

## 3. Limitar la Memoria Máxima de WSL 2 (`.wslconfig`)

Para garantizar que la máquina virtual de contenedores no acapare toda la memoria RAM de Windows (y respete el presupuesto de $\le 6\text{ GB}$ del curso), cree o edite el archivo `$env:USERPROFILE\.wslconfig` desde PowerShell:

```powershell
@"
[wsl2]
memory=6GB
processors=4
swap=2GB
"@ | Set-Content -Path "$env:USERPROFILE\.wslconfig" -Encoding UTF8
```

---

## 4. Instalar Docker Desktop (o Alternativa Libre)

### Opción A: Docker Desktop (Estándar en las guías)

1. Descargue **Docker Desktop for Windows** desde https://www.docker.com/products/docker-desktop/.
2. Durante la instalación, asegúrese de dejar marcada la casilla **Use WSL 2 instead of Hyper-V (recommended)**.
3. Inicie Docker Desktop, abra **Settings > General** y confirme que **Use the WSL 2 based engine** esté activado.

![captura: Configuración General de Docker Desktop con motor WSL 2 activado](img/02-docker-desktop-wsl2.png)

> [!NOTE]
> **Nota importante sobre licenciamiento de Docker Desktop:**
> Docker Desktop es gratuito para uso personal, educativo y pequeñas empresas (Docker Personal), pero requiere una suscripción de pago en organizaciones gubernamentales o empresas con más de 250 empleados o más de USD 10 millones de ingresos anuales. Si trabaja desde un equipo institucional sujeto a esta restricción, utilice cualquiera de las dos alternativas 100 % gratuitas y de código abierto descritas en la **Opción B**.

### Opción B: Alternativas Libres (Rancher Desktop o Podman Desktop)

- **Rancher Desktop (Recomendado como alternativa 1:1):**
  1. Descárguelo desde https://rancherdesktop.io/.
  2. En el asistente inicial, elija como motor de contenedores **`dockerd (moby)`** (en lugar de `containerd`).
  3. Esto habilita directamente los comandos `docker` y `docker compose` en PowerShell, siendo 100 % compatible con todos los scripts `.ps1`, `.sh` y con **Testcontainers** sin ningún cambio adicional.
- **Podman Desktop:**
  1. Descárguelo desde https://podman-desktop.io/.
  2. Active la opción *Docker compatibility* para exponer el socket compatible con Docker y `docker compose`.

---

## 5. Verificar el Funcionamiento de Docker y Docker Compose

Abra una terminal de **PowerShell** (sin privilegios de administrador) y ejecute:

```powershell
docker --version
docker compose version
docker run --rm hello-world
```

Salida esperada en el tercer comando:

```text
Hello from Docker!
This message shows that your installation appears to be working correctly.
```

![captura: Ejecución exitosa de docker run --rm hello-world en PowerShell](img/02-docker-hello-world.png)

---

**Siguiente paso:** Continúe con [03 — JDK 21 y Spring Tools for Eclipse](03-jdk-y-eclipse.md).

