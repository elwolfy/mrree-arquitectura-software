# Paso 3: Instalación de JDK 21 (Eclipse Temurin) y Spring Tools for Eclipse

El código fuente del backend de los cuatro módulos está escrito en **Java 21 LTS**. Aunque la compilación en contenedores utiliza imágenes con JDK 21 integrado, instalar **Eclipse Temurin 21** y **Spring Tools 4 for Eclipse** en su máquina le permitirá explorar las clases con autocompletado, ejecutar pruebas unitarias/ArchUnit con un clic y depurar microservicios en modo híbrido.

---

## 1. Instalar Eclipse Temurin JDK 21 LTS

1. Ingrese a https://adoptium.net/temurin/releases/?version=21.
2. Descargue el instalador `.msi` de **JDK 21 (LTS)** para Windows x64.
3. En el asistente de instalación, asegúrese de activar la opción **Set or override JAVA_HOME variable** (por defecto desactivada con una `X`; cámbiela a *Will be installed on local hard drive*).

![captura: Instalador de Eclipse Temurin JDK 21 con la opción Set JAVA_HOME seleccionada](img/03-temurin-21-java-home.png)

4. Abra una **nueva** ventana de PowerShell y verifique que Java 21 esté activo en su ruta:

```powershell
java -version
echo $env:JAVA_HOME
```

Salida esperada:

```text
openjdk version "21.0.5" 2024-10-15 LTS
OpenJDK Runtime Environment Temurin-21.0.5+11 (build 21.0.5+11-LTS)
OpenJDK 64-Bit Server VM Temurin-21.0.5+11 (build 21.0.5+11-LTS, mixed mode, sharing)
```

---

## 2. Instalar Spring Tools 4 for Eclipse (STS)

1. Descargue **Spring Tools 4 for Eclipse** para Windows x86_64 desde https://spring.io/tools.
2. El archivo descargado es un ejecutable autoextraíble (`spring-tool-suite-4-...-win32.win32.x86_64.self-extracting.jar` o `.exe`).
3. Extraiga la carpeta `sts-4.x.x.RELEASE` en una ruta corta sin espacios ni caracteres especiales (por ejemplo, `C:\herramientas\sts-4`).
4. Ejecute `SpringToolSuite4.exe` y elija una carpeta de *Workspace* dedicada (por ejemplo, `C:\workspaces\mrree-arquitectura`).

![captura: Selección de Workspace al iniciar Spring Tools 4 for Eclipse](img/03-sts-workspace.png)

---

## 3. Configurar JDK 21 como JRE Predeterminado en Eclipse

1. En la barra de menú de Eclipse, abra **Window > Preferences**.
2. Navegue a **Java > Installed JREs**:
   - Verifique que aparezca el **JDK 21 (Eclipse Temurin)**. Si solo aparece el JRE embebido de Eclipse, haga clic en **Add... > Standard VM**, seleccione el directorio `C:\Program Files\Eclipse Adoptium\jdk-21...`, márquelo con un check como predeterminado y pulse **Apply**.
3. Navegue a **Java > Compiler** y verifique que **Compiler compliance level** esté en **`21`**.
4. Navegue a **General > Workspace** y confirme que:
   - **Text file encoding** sea **UTF-8**.
   - **New text file line delimiter** esté en **Other: Unix (LF)**.

![captura: Preferencias de Eclipse mostrando Installed JREs con Eclipse Temurin JDK 21 seleccionado](img/03-eclipse-installed-jres.png)

---

## 4. Importar los Proyectos Maven de un Módulo

Cada módulo (`m1-estilos`, `m2-integracion`, `m3-datos`, `m4-observabilidad`) posee su propio `pom.xml` reactor independiente. Para evitar confusión entre servicios con el mismo nombre en distintos módulos (por ejemplo, `tramites-service` de M2 vs. `tramites-service` de M3), **importe en Eclipse únicamente el módulo con el que esté trabajando en la sesión actual**:

1. En Eclipse, seleccione **File > Import... > Maven > Existing Maven Projects** y haga clic en **Next**.
2. En **Root Directory**, haga clic en **Browse...** y seleccione la carpeta del módulo correspondiente dentro de su repositorio clonado (por ejemplo, `C:\repos\mrree-arquitectura-software\m1-estilos`).
3. Verá el `pom.xml` raíz del módulo y todos sus submódulos seleccionados. Haga clic en **Finish**.
4. Espere a que la barra inferior derecha (*Building workspace / Importing Maven projects*) llegue al 100 %.

![captura: Asistente Import Existing Maven Projects con los submódulos de m1-estilos seleccionados](img/03-eclipse-import-maven.png)

> [!TIP]
> Cuando pase al siguiente módulo del curso (por ejemplo, de `m1-estilos` a `m2-integracion`), puede cerrar los proyectos del módulo anterior en el *Package Explorer* (*clic derecho > Close Project*) antes de importar los del nuevo módulo.

---

**Siguiente paso:** Continúe con [04 — Fork, Clonar y Ejecutar el Primer Módulo](04-fork-clonar-ejecutar.md).

